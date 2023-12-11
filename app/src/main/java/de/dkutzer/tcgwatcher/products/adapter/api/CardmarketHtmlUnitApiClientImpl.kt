package de.dkutzer.tcgwatcher.products.adapter.api

import android.util.Log
import de.dkutzer.tcgwatcher.products.config.BaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import org.htmlunit.BrowserVersion
import org.htmlunit.NicelyResynchronizingAjaxController
import org.htmlunit.WebClient
import org.htmlunit.WebRequest
import org.htmlunit.html.HtmlPage
import org.htmlunit.javascript.SilentJavaScriptErrorListener
import org.htmlunit.util.NameValuePair
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class CardmarketHtmlUnitApiClientImpl(val config: BaseConfig) : ProductApiClient {

    val paginationRegex = "\\b(?:von|of|de) (\\d+)\\b".toRegex()

    /*
    CM is protected by CloudFlare.
    Which means we get a 403 sometimes... Which is annoying. The Internet says
    htmlunit is the thing when its about bypassing this...at least for JVM apps.
    But I think its still very tricky. CF knows what its doing.

    I can totally understand that CM doesnt want scraping but ffs just open your ReST API
    and you will have much more control over stuff like this app.
    Use Oauth2/OpenId instead of this multiple onion like token/secret/pass crap the next time.
     */

    override suspend fun search(
        searchString: String,
        page: Int
    ): SearchResultsPageDto {

        /*
        I searched half the internet for the best way to use htmlunit to bypass cloudflare
        but it not working always.
        We get some JS Exceptions which dont come up in a Browser. I guess I can ignore them.

         */
        logger.debug { "Searching for $searchString with page: $page" }
        WebClient(BrowserVersion.CHROME).use { webClient ->
            modifiyWebClient(webClient)
            val params = mapOf(
                "searchString" to searchString,
                /* "sortBy" to "price_asc",*/
                "perSite" to "5",
                "mode" to "gallery",
//                "language" to "3", //german -- TODO: the language is set via the path
                "site" to "$page"
            )
            val webRequest = createWebRequest(
                config.searchUrl,
                //sorting defaults to popularity which is the best i think
                params
            )
            webClient.waitForBackgroundJavaScript(5000)

            logger.debug { "Executing Request now" }
            lateinit var htmlPage: HtmlPage
            val duration = measureTimeMillis {
                htmlPage = webClient.getPage(webRequest)
                webClient.waitForBackgroundJavaScript(5000)
                //this should trick CF to do its thing and refresh the page before we parse it
                //sadly i couldnt find a way to log this behaviour
                htmlPage.enclosingWindow.jobManager.waitForJobs(5000)
            }
            logger.debug { "Duration: $duration" }


            logger.info { "Status: ${htmlPage.webResponse.statusCode}" }

            val document = Jsoup.parse(htmlPage.asXml())

            return parseGallerySearchResults(document, page)


        }

    }

    private fun parseGallerySearchResults(document: Document, page: Int): SearchResultsPageDto {
        logger.debug { "Parsing a tags with class card and a href" }
        val tiles = document.getElementsByTag("a").filter { element ->  element.hasClass("card") && element.hasAttr("href") }
        logger.debug { "Found: ${tiles.size}" }

        val searchResultItemDtos = ArrayList<SearchResultItemDto>(tiles.size)

        tiles.forEach {
            logger.debug { "Parsing: $it" }
            val cmLink = it.attr("href")
            logger.info { "link: $cmLink" }

            val imgTag = it.getElementsByTag("img")
            logger.debug { "ImgTag: $imgTag" }
            val imageLink = imgTag.attr("data-echo")
            logger.info { "Image Link: $imageLink" }

            val titleTag = it.getElementsByTag("h2")
            logger.debug { "TitleTag: $titleTag" }
            val localName = titleTag.text()
            logger.debug { "Local Name: $localName" }

            val intPriceTag = it.getElementsByTag("b")
            logger.debug { "Found intPriceTag: $intPriceTag" }
            val intPrice = intPriceTag.text()
            logger.debug { "Price: $intPrice" }

            val itemDto = SearchResultItemDto(
                displayName = localName,
                orgName = "",
                cmLink = cmLink,
                imgLink = imageLink,
                price = intPrice
            )

            searchResultItemDtos.add(itemDto)

        }
        val totalPages = parsePageination(document)


        return SearchResultsPageDto(searchResultItemDtos, page, totalPages)

    }

    private fun parsePageination(document: Document): Int {
        logger.debug { "Looking for Pagination info" }
        val paginationDiv = document.getElementById("pagination")
        logger.debug { paginationDiv }
        val paginationSpans = paginationDiv?.getElementsByTag("span")
        logger.debug { "Spans: $paginationSpans" }
        val paginationSpan = paginationSpans?.first { s -> s.hasClass("mx-1") }
        logger.debug { "mxSpan: $paginationSpan" }

        var groupValue:  String? = null
        if(paginationSpan!=null) {
            val text = paginationSpan.text()
            logger.debug { "Text: $text" }
            val matchResult = paginationRegex.find(text)
            groupValue = matchResult?.groupValues?.getOrNull(1)
            logger.debug { "$groupValue" }
        }

        val totalPages = groupValue?.toInt() ?: 0
        logger.info { "Found: $totalPages" }
        return totalPages
    }

    private fun modifiyWebClient(webClient: WebClient) {
        webClient.options.isJavaScriptEnabled = true
        webClient.options.isCssEnabled = false
        webClient.options.isRedirectEnabled = true
        webClient.cache.maxSize = 0
        webClient.ajaxController = NicelyResynchronizingAjaxController()
        webClient.options.isThrowExceptionOnFailingStatusCode = false
        webClient.javaScriptErrorListener = SilentJavaScriptErrorListener()
        webClient.waitForBackgroundJavaScript(10000)
        webClient.javaScriptTimeout = 10000
        webClient.waitForBackgroundJavaScriptStartingBefore(10000)
        //webClient.htmlParserListener = HTMLParserListener.LOG_REPORTER //just a bunch of useless error messages
        webClient.options.isThrowExceptionOnScriptError = false
        webClient.addRequestHeader(HttpHeaders.Accept, "text/html")
        webClient.addRequestHeader(
            HttpHeaders.AcceptLanguage,
            "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7"
        )
    }

    private fun createWebRequest(url: String, params: Map<String, String>): WebRequest {
        val webRequest = WebRequest(URL(url), "text/html", "gzip, deflate")
        if (params.isNotEmpty()) {
            webRequest.requestParameters =
                params.map { p -> NameValuePair(p.key, p.value) }.toList()
        }
        return webRequest
    }


    override suspend fun getProductDetails(link: String): ProductDetailsDto {

        WebClient(BrowserVersion.CHROME).use { webClient ->
            modifiyWebClient(webClient)
            val webRequest = createWebRequest(
                "${config.baseUrl}$link",
                mapOf()
            )
            webClient.waitForBackgroundJavaScript(5000)

            val htmlPage: HtmlPage = webClient.getPage(webRequest)

            webClient.waitForBackgroundJavaScript(5000)
            htmlPage.enclosingWindow.jobManager.waitForJobs(5000)

            println("Status: ${htmlPage.webResponse.statusCode}")

            val document = Jsoup.parse(htmlPage.asXml())

            val imageTags = document.getElementsByTag("img")
            val frontImageTag =
                imageTags.first { img -> img.classNames().size == 1 } //filter out "lazy" img tags
            val imageUrl = frontImageTag.attr("src")

            val infoDivs = document.getElementsByClass("info-list-container")
            var localPrice = "0,00 €"
            var localPriceTrend = "0,00 €"
            if (infoDivs.size == 1) {
                val infoDiv = infoDivs.first()
                val dts = infoDiv?.getElementsByTag("dt")
                val abDt = dts?.first { dt -> dt.text().equals("ab") }
                val abDd = abDt?.nextElementSibling()
                if (abDd != null) {
                    localPrice = abDd.text()
                }

                val priceTrendDt = dts?.first { dt -> dt.text().equals("Preis-Trend") }
                val priceTrendDd = priceTrendDt?.nextElementSibling()
                if (priceTrendDd != null) {
                    val span = priceTrendDd.getElementsByTag("span")
                    localPriceTrend = span.text()
                }

            }

            return ProductDetailsDto(imageUrl, localPrice, localPriceTrend)
        }
    }
}