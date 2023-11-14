package de.dkutzer.tcgwatcher.products.adapter

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import de.dkutzer.tcgwatcher.products.adapter.api.ProductCardmarketRepository
import de.dkutzer.tcgwatcher.products.config.BaseConfig
import de.dkutzer.tcgwatcher.products.domain.port.ProductDetails
import de.dkutzer.tcgwatcher.products.domain.port.SearchItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import it.skrape.core.htmlDocument
import it.skrape.matchers.isNumeric
import it.skrape.selects.and
import it.skrape.selects.html5.a
import it.skrape.selects.html5.body
import it.skrape.selects.html5.div
import it.skrape.selects.html5.main
import it.skrape.selects.html5.section
import kotlinx.coroutines.runBlocking
import java.util.Locale

class ProductCardmarketRepositoryAdapter(val config: BaseConfig) : ProductCardmarketRepository {


    private var client: HttpClient =
        HttpClient(OkHttp) {
            //OkHttp seems to be the only enigne which can handle multiply set-cookie header
            followRedirects = false

            install(Logging)
            {
                level = LogLevel.ALL
            }
            BrowserUserAgent() //ihnalt egal, dard nur nich leer sein

        }

    override fun getProductDetailsById(id: String): ProductDetails {

        TODO("Not yet implemented")
    }

    override fun search(searchString: String): List<SearchItem> {

        return runSearch(searchString)

    }

    private fun runSearch(searchString: String): List<SearchItem> {

        //minimum working example
        //curl -vvvv  'https://www.cardmarket.com/de/Pokemon/Products/Search?searchString=%5BGlu%5D&exactMatch=on&onlyAvailable=on&idRarity=0&sortBy=price_asc&perSite=20' \
        //             https://www.cardmarket.com/de/Pokemon/Products/Search?idCategory=0&idExpansion=0&searchString=Glu&exactMatch=on&onlyAvailable=on&idRarity=0&sortBy=price_asc&perSite=20
        //https://www.cardmarket.com/de/Pokemon/Products/Singles/Astral-Radiance?searchString=%5BBisaflor%5D&exactMatch=on&onlyAvailable=on&idRarity=0&sortBy=price_asc&perSite=30
        //https://www.cardmarket.com/de/Pokemon/Products/Singles?idExpansion=0&searchString=%5BBisaflor%5D&exactMatch=on&onlyAvailable=on&idRarity=0&sortBy=price_asc&perSite=20
        // -H 'accept: text/html' \
        // -H 'accept-language: de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7' \
        // -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36'
        val searchItems : MutableList<SearchItem>  = mutableListOf()

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
            println("Status: ${response.status}")
            //println("Body: ${response.bodyAsText()}")

            val bodyAsText = response.bodyAsText()
            htmlDocument(bodyAsText) {
                relaxed = true

                val rows = div {
                    withClass = "row" and  "no-gutters"
                    withAttributeKey = "id"
                    findAll{ this }
                }
                rows.forEach { row ->
                    if(!row.id.contains("pagination")) {

                        val innerRow = row.div {
                            withClass = "row" and "no-gutters"
                            findFirst { this }
                        }
                        val col1 = innerRow.div {
                            withClass = "col-12" and "col-md-8"
                            findFirst { this }
                        }
                        val linkDiv = col1.div {
                            //no selector
                            findFirst {
                                this
                            }
                        }

                        val link = linkDiv.eachHref.getOrElse(0, defaultValue = {""})
                        val name = linkDiv.text
                        val orgNameDiv = col1.div {
                            withClass = "d-block" and "small" and "text-muted" and "fst-italic"
                            findFirst { this }
                        }

                        val orgName = orgNameDiv.text

                        val priceDiv = row.div {
                            withClass = "col-price" and "pe-sm-2"
                            findFirst{
                                this
                            }
                        }
                        var price = priceDiv.text.toDoubleOrNull()
                        if(price==null)
                            price = 0.0

                        val currencyAmount = CurrencyAmount(
                            price,
                            Currency.getInstance(Locale.getDefault())
                        )
                        val searchItem = SearchItem(cmLink = link, displayName = name, orgName = orgName, price = currencyAmount)

                        searchItems.add(searchItem)

                    }

                }

            }
        }


        return searchItems
    }
}

