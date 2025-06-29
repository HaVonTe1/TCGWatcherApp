package de.dkutzer.tcgwatcher.collectables.search.data.cardmarket

import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto
import de.dkutzer.tcgwatcher.settings.domain.BaseConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import org.jsoup.Jsoup


class CardmarketKtorApiClientImpl(val config: BaseConfig) : BaseCardmarketApiClient() {



    override suspend fun search(searchString: String, page: Int): SearchResultsPageDto {

        HttpClient {
            followRedirects = true


            BrowserUserAgent()

        }.use { client ->
            val response = client.get(config.searchUrl) {
                url {
                    //This should take care of url encoding
                    parameters.append("searchString", searchString)
                    //parameters.append("sortBy", "price_asc")
                    parameters.append("perSite", config.limit.toString()) // limit
                    parameters.append("mode", "gallery")
                    parameters.append("site", "$page") //offset = limit * page

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



    override suspend fun getProductDetails(link: String): CardmarketProductDetailsDto {
        HttpClient(OkHttp) {
            followRedirects = true


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

            return parseProductDetails(document, "todo")
        }
    }
}