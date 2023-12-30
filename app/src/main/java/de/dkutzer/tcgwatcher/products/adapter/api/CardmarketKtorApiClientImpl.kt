package de.dkutzer.tcgwatcher.products.adapter.api



import de.dkutzer.tcgwatcher.products.config.BaseConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import org.jsoup.Jsoup

class CardmarketKtorApiClientImpl(val config: BaseConfig) : BaseCardmarketApiClient() {


    override suspend fun search(searchString: String, page: Int): SearchResultsPageDto {

        HttpClient(OkHttp) {
            //OkHttp seems to be the only enigne which can handle multiply set-cookie header
            followRedirects = false

            install(Logging)
            {
                level = LogLevel.ALL
            }
            BrowserUserAgent() //ihnalt egal, dard nur nich leer sein

        }.use { client ->
            val response = client.get(config.searchUrl) {
                url {
                    //This should take care of url encoding
                    parameters.append("searchString", searchString)
                    //parameters.append("sortBy", "price_asc")
                    parameters.append("perSite", "5")
                    parameters.append("mode", "gallery")
                    parameters.append("site", "$page")

                }
                headers {
                    append(HttpHeaders.Accept, "text/html")
                    append(HttpHeaders.AcceptLanguage, "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")

                }
            }
            val bodyAsText = response.bodyAsText()

            val document = Jsoup.parse(bodyAsText)

            return parseGallerySearchResults(document, page)
        }



    }

    override suspend fun getProductDetails(link: String): ProductDetailsDto {
        HttpClient(OkHttp) {
            //OkHttp seems to be the only enigne which can handle multiply set-cookie header
            followRedirects = false

            install(Logging)
            {
                level = LogLevel.ALL
            }
            BrowserUserAgent() //ihnalt egal, dard nur nich leer sein

        }.use { client ->
            val response = client.get("${config.baseUrl}$link") {
                headers {
                    append(HttpHeaders.Accept, "text/html")
                    append(HttpHeaders.AcceptLanguage, "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")
                }
            }
            val bodyAsText = response.bodyAsText()
            val document = Jsoup.parse(bodyAsText)

            return parseProductDetails(document)
        }
    }
}

