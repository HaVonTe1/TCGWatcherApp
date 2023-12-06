package de.dkutzer.tcgwatcher.products.adapter.api

import de.dkutzer.tcgwatcher.products.config.BaseConfig
import io.ktor.http.HttpHeaders
import org.htmlunit.BrowserVersion
import org.htmlunit.NicelyResynchronizingAjaxController
import org.htmlunit.WebClient
import org.htmlunit.WebRequest
import org.htmlunit.html.HtmlPage
import org.htmlunit.javascript.SilentJavaScriptErrorListener
import org.htmlunit.util.NameValuePair
import org.jsoup.Jsoup
import java.net.URL


class CardmarketHtmlUnitApiClientImpl (val config: BaseConfig) : ProductApiClient {

    val paginationRegex = "/^.+ \\d{1,3} .+ (\\d{1,3})/gm".toRegex()

    /*
    CM is protected by CloudFlare.
    Which means we get a 403 sometimes... Which is annoying. The Internet says
    htmlunit is the thing when its about bypassing this...at least for JVM apps.
    But I think its still very tricky. CF knows what its doing.

    I can totally understand that CM doesnt want scraping but ffs just open your ReST API
    and you will have much more control over stuff like this app. Use Oauth2/OpenId  the next time.
     */

    override fun search(searchString: String, page: Int): SearchResultsPageDto {

           /*
           I searched half the internet for the best way to use htmlunit to bypass cloudflare
           but it not working always.
           We get some JS Exceptions which dont come up in a Browser. I guess I can ignore them.

            */
           WebClient(BrowserVersion.CHROME).use { webClient ->
               modifiyWebClient(webClient)
               val webRequest =  createWebRequest(
                   config.searchUrl,
                   mapOf("searchString" to searchString, "sortBy" to "price_asc", "perSite" to "5", "site" to "$page")
               )
               webClient.waitForBackgroundJavaScript(5000)

               val htmlPage: HtmlPage = webClient.getPage(webRequest)

               //this should trick CF to do its thing and refresh the page before we parse it
               //sadly i couldnt find a way to log this behaviour
               webClient.waitForBackgroundJavaScript(5000)
               htmlPage.enclosingWindow.jobManager.waitForJobs(5000)

               println("Status: ${htmlPage.webResponse.statusCode}")

               val document = Jsoup.parse(htmlPage.asXml())

               //this parses should be imune to html rewriting
               val rows = document.getElementsByClass("row")

               val productRows = rows.filter { row -> row.id().startsWith("productRow") }.toList()
               val searchResultItemDtos = ArrayList<SearchResultItemDto>(productRows.size)

               productRows.forEach { row ->
                   var displayName= "";
                   var orgName = ""
                   var cmLink = ""
                   var price = ""

                   val nameAndLinkColDivs = row.getElementsByClass("col").filter { div -> div.classNames().size==1 } //filter first col with "d-none" class

                   val nameAndLinkColDiv = nameAndLinkColDivs.firstOrNull()
                   if(nameAndLinkColDiv!=null) {
                       val aRefs = nameAndLinkColDiv.getElementsByTag("a")
                       if(aRefs.size==1) {
                           val aRef = aRefs.first()
                           if (aRef != null) {
                               displayName = aRef.text()
                               cmLink = aRef.attr("href")
                               val firstElementSibling = aRef.nextElementSibling()
                               if (firstElementSibling != null) {
                                   orgName= firstElementSibling.text()
                               }

                           }
                       }
                   }

                   val priceCols = row.getElementsByClass("col-price")
                   if(priceCols.size>0) {
                       val priceCol = priceCols.first()
                       if (priceCol != null) {
                           price = priceCol.text()
                       }
                   }

                   val itemDto =
                       SearchResultItemDto(displayName, orgName, cmLink, price)

                   searchResultItemDtos.add(itemDto)

               }
               val paginationDiv = document.getElementById("pagination")
               val paginationSpans = paginationDiv?.getElementsByTag("span")
               val paginationSpan = paginationSpans?.first { s -> s.hasClass("mx-1") }
               val groupValues = paginationSpan?.let { paginationRegex.find(it.text())?.groupValues }
               val totalPages = groupValues?.firstOrNull()?.toInt() ?: 0

               return SearchResultsPageDto(searchResultItemDtos, page , totalPages )
           }

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

    private fun createWebRequest(url: String, params: Map<String, String>) : WebRequest {
        val webRequest = WebRequest(URL(url), "text/html", "gzip, deflate")
        if(params.isNotEmpty()) {
            webRequest.requestParameters = params.map { p -> NameValuePair(p.key,p.value) }.toList()
        }
        return webRequest
    }


    override fun getProductDetails(link: String): ProductDetailsDto {
        WebClient(BrowserVersion.CHROME).use { webClient ->
            modifiyWebClient(webClient)
            val webRequest = createWebRequest(
                link,
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
            if(infoDivs.size==1) {
                val infoDiv = infoDivs.first()
                val dts = infoDiv?.getElementsByTag("dt")
                val abDt = dts?.first { dt -> dt.text().equals("ab") }
                val abDd = abDt?.nextElementSibling()
                if (abDd != null) {
                    localPrice=  abDd.text()
                }

                val priceTrendDt = dts?.first { dt -> dt.text().equals("Preis-Trend") }
                val priceTrendDd = priceTrendDt?.nextElementSibling()
                if(priceTrendDd!=null) {
                    val span = priceTrendDd.getElementsByTag("span")
                    localPriceTrend = span.text()
                }

            }

            return ProductDetailsDto(imageUrl, localPrice, localPriceTrend)
        }
    }
}