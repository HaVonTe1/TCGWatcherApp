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

class CardmarketHtmlUnitApiClientImpl(val config: BaseConfig) : BaseCardmarketApiClient() ,  ProductApiClient {


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

    private fun modifiyWebClient(webClient: WebClient) {
        webClient.options.isJavaScriptEnabled = false
        webClient.options.isCssEnabled = false
        webClient.options.isRedirectEnabled = false
        webClient.options.isAppletEnabled = false
        webClient.options.isDownloadImages = false
        webClient.options.isWebSocketEnabled = false
        webClient.options.isDoNotTrackEnabled = true
        webClient.options.isPopupBlockerEnabled = true
        webClient.options.isGeolocationEnabled = false

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

            return parseProductDetails(document)
        }
    }


}