package de.dkutzer.tcgwatcher.cards.boundary

import de.dkutzer.tcgwatcher.cards.control.toSearchResultItemDto
import de.dkutzer.tcgwatcher.cards.entity.BaseConfig
import de.dkutzer.tcgwatcher.cards.entity.CardDetailsDto
import de.dkutzer.tcgwatcher.cards.entity.SearchResultsPageDto
import de.dkutzer.tcgwatcher.settings.entity.Engines
import io.github.oshai.kotlinlogging.KotlinLogging
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
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class CardmarketHtmlUnitApiClientImpl(val config: BaseConfig) : BaseCardmarketApiClient() {
    /*
    CM is protected by CloudFlare.
    Which means we get a 403 sometimes... Which is annoying. The Internet says
    htmlunit is the thing when its about bypassing this...at least for JVM apps.
    But I think its still very tricky. CF knows what its doing.

    I can totally understand that CM doesnt want scraping but ffs just open your ReST API
    and you will have much more control over stuff like this app.
    Use Oauth2/OpenId instead of this multiple onion like token/secret/pass crap the next time.
     */
    companion object {
        const val WAIT_TIME = 5000L
        const val JS_TIMEOUT = 10000L
    }

    /*
    I searched half the internet for the best way to use htmlunit to bypass cloudflare
    but it not working always.
    We get some JS Exceptions which dont come up in a Browser. I guess I can ignore them.

     */
    override suspend fun search(searchString: String, page: Int): SearchResultsPageDto {
        logger.debug { "Searching for $searchString with page: $page" }
        try {
            WebClient(BrowserVersion.CHROME).use { webClient ->
                modifyWebClient(webClient)
                val params = mapOf(
                    "searchString" to searchString,
                    "perSite" to config.limit.toString(),
                    "mode" to "gallery",
                    "site" to "$page"
                )
                val webRequest = createWebRequest(config.searchUrl, params)
                webClient.waitForBackgroundJavaScript(WAIT_TIME)

                logger.debug { "Executing Request now" }
                val htmlPage: HtmlPage?
                val duration = measureTimeMillis {
                    htmlPage = webClient.getPage(webRequest)
                    webClient.waitForBackgroundJavaScript(WAIT_TIME)
                    //this should trick CF to do its thing and refresh the page before we parse it
                    //sadly i couldnt find a way to log this behaviour
                    htmlPage.enclosingWindow.jobManager.waitForJobs(WAIT_TIME)
                }
                logger.info { "Duration: $duration" }

                htmlPage?.let {
                    logger.debug { "LoadTime: ${it.webResponse.loadTime}" }
                    logger.debug { "Status: ${it.webResponse.statusCode}" }
                    val url = it.webResponse.webRequest.url
                    logger.debug { "Url: $url" }

                    val document = Jsoup.parse(it.asXml())

                    if(url.path.contains("Singles")) {
                        val productDetails = parseProductDetails(document, url.path)
                        val searchResultsPageDto = SearchResultsPageDto(
                            listOf(productDetails.toSearchResultItemDto()),
                            page,
                            1
                        )
                        return searchResultsPageDto
                    } else
                    {
                        return parseGallerySearchResults(document, page)
                    }
                } ?: run {
                    logger.error { "Failed to load page" }
                    throw IllegalStateException("HtmlPage is null")
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error during search" }
            throw e
        }
    }


    private fun modifyWebClient(webClient: WebClient) {
        webClient.options.apply {
            isJavaScriptEnabled = config.engine == Engines.HTMLUNIT_JS
            isCssEnabled = false
            isRedirectEnabled = true
            isDownloadImages = false
            isWebSocketEnabled = false
            isDoNotTrackEnabled = true
            isPopupBlockerEnabled = true
            isGeolocationEnabled = false
            isThrowExceptionOnFailingStatusCode = false
            isThrowExceptionOnScriptError = false
        }
        webClient.cache.maxSize = 0
        webClient.ajaxController = NicelyResynchronizingAjaxController()
        webClient.javaScriptErrorListener = SilentJavaScriptErrorListener()
        webClient.waitForBackgroundJavaScript(JS_TIMEOUT)
        webClient.javaScriptTimeout = JS_TIMEOUT
        webClient.waitForBackgroundJavaScriptStartingBefore(JS_TIMEOUT)
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

    override suspend fun getProductDetails(link: String): CardDetailsDto {
        logger.debug { "CMHtmlUnitApiClientImpl: getProductDetails: $link" }
        try {
            WebClient(BrowserVersion.CHROME).use { webClient ->
                modifyWebClient(webClient)
                val webRequest = createWebRequest("${config.baseUrl}$link", mapOf())
                webClient.waitForBackgroundJavaScript(WAIT_TIME)

                val htmlPage: HtmlPage? = webClient.getPage(webRequest)
                webClient.waitForBackgroundJavaScript(WAIT_TIME)
                htmlPage?.enclosingWindow?.jobManager?.waitForJobs(WAIT_TIME)

                htmlPage?.let {
                    logger.debug { "Status: ${it.webResponse.statusCode}" }
                    val document = Jsoup.parse(it.asXml())
                    return parseProductDetails(document,link)
                } ?: run {
                    logger.error { "Failed to load product details page" }
                    throw IllegalStateException("HtmlPage is null")
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error during getProductDetails" }
            throw e
        }
    }
}
