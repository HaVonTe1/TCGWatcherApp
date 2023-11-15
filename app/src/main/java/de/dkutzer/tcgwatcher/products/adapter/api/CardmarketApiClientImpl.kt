package de.dkutzer.tcgwatcher.products.adapter.api

import de.dkutzer.tcgwatcher.products.config.BaseConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import kotlinx.coroutines.runBlocking

class CardmarketApiClientImpl(val config: BaseConfig) : ProductApiClient {
    private var client: HttpClient =
        HttpClient(OkHttp) {
            //OkHttp seems to be the only enigne which can handle multiply set-cookie header
            followRedirects = false

//            install(Logging)
//            {
//                level = LogLevel.ALL
//            }
          //  BrowserUserAgent() //ihnalt egal, dard nur nich leer sein

        }
    override fun search(searchString: String): SearchItemsDto {

        //minimum working example
        //curl -vvvv  'https://www.cardmarket.com/de/Pokemon/Products/Search?searchString=%5BGlu%5D&exactMatch=on&onlyAvailable=on&idRarity=0&sortBy=price_asc&perSite=20' \
        //             https://www.cardmarket.com/de/Pokemon/Products/Search?idCategory=0&idExpansion=0&searchString=Glu&exactMatch=on&onlyAvailable=on&idRarity=0&sortBy=price_asc&perSite=20
        //https://www.cardmarket.com/de/Pokemon/Products/Singles/Astral-Radiance?searchString=%5BBisaflor%5D&exactMatch=on&onlyAvailable=on&idRarity=0&sortBy=price_asc&perSite=30
        //https://www.cardmarket.com/de/Pokemon/Products/Singles?idExpansion=0&searchString=%5BBisaflor%5D&exactMatch=on&onlyAvailable=on&idRarity=0&sortBy=price_asc&perSite=20
        // -H 'accept: text/html' \
        // -H 'accept-language: de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7' \
        // -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36'


        var searchItemsDto : SearchItemsDto
        runBlocking {
            val response = client.get(config.searchUrl) {
                url {
                    //This should take care of url encoding
                    parameters.append("searchString", searchString)
                    parameters.append("sortBy", "price_asc")
                    parameters.append("perSite", "5")

                }
                headers {
                    append(HttpHeaders.Accept, "text/html")
                    append(HttpHeaders.AcceptLanguage, "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")

                }
            }
            searchItemsDto = SearchItemsDto(response.bodyAsText())
            client.close()
        }
        return searchItemsDto

    }

    override fun getProductDetails(id: String): ProductDetailsDto {
        TODO("Not yet implemented")
    }


}

