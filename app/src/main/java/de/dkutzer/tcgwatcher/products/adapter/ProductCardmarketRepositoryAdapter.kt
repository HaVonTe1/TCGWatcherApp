package de.dkutzer.tcgwatcher.products.adapter

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import de.dkutzer.tcgwatcher.products.adapter.api.ProductApiClient
import de.dkutzer.tcgwatcher.products.adapter.port.ProductRepository
import de.dkutzer.tcgwatcher.products.domain.model.ProductModel
import de.dkutzer.tcgwatcher.products.domain.model.SearchItem
import de.dkutzer.tcgwatcher.products.domain.port.ProductDetails
import it.skrape.core.htmlDocument
import it.skrape.selects.and
import it.skrape.selects.html5.div
import it.skrape.selects.html5.img
import java.util.Locale

class ProductCardmarketRepositoryAdapter(val client: ProductApiClient) :
    ProductRepository {


    override fun getProductImageUrlById(link: String): String {
/*
Examples:
https://www.cardmarket.com/de/Pokemon/Products/Singles/151/Mew-ex-V1-MEW151
https://www.cardmarket.com/de/Pokemon/Products/Singles/Paradox-Rift/Earthen-Vessel-PAR163
https://www.cardmarket.com/de/Pokemon/Products/Singles/151/Blastoise-ex-V1-MEW009
 */

        var imageUrl = "";
        val productDetails = client.getProductDetails(link)
        htmlDocument(productDetails.htmlCode) {
            relaxed = true

            val mainContent = div {
                withId = "mainContent"
                findFirst { this }
            }
            val slideCardDiv = mainContent.div {
                withClass = "image" and "card-image" and "is-pokemon" and "has-shadow"
                findSecond { this }
            }
            val image = slideCardDiv.img {
                findFirst { this }
            }
            imageUrl = image.attribute("src")

        }

        return imageUrl
    }

    override fun search(searchString: String): List<SearchItem> {

        val searchItems: MutableList<SearchItem> = mutableListOf()

        val bodyAsText = client.search(searchString).htmlCode
        htmlDocument(bodyAsText) {
            relaxed = true

            val rows = div {
                withClass = "row" and "no-gutters"
                withAttributeKey = "id"
                findAll { this }
            }
            rows.forEach { row ->
                if (!row.id.contains("pagination")) {

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

                    val link = linkDiv.eachHref.getOrElse(0, defaultValue = { "" })
                    val name = linkDiv.text
                    val orgNameDiv = col1.div {
                        withClass = "d-block" and "small" and "text-muted" and "fst-italic"
                        findFirst { this }
                    }

                    val orgName = orgNameDiv.text

                    val priceDiv = row.div {
                        withClass = "col-price" and "pe-sm-2"
                        findFirst {
                            this
                        }
                    }
                    var price = priceDiv.text.toDoubleOrNull()
                    if (price == null)
                        price = 0.0

                    val currencyAmount = CurrencyAmount(
                        price,
                        Currency.getInstance(Locale.getDefault())
                    )
                    val searchItem = SearchItem(
                        cmLink = link,
                        displayName = name,
                        orgName = orgName,
                        price = currencyAmount
                    )

                    searchItems.add(searchItem)

                }

            }

        }
        return searchItems
    }
}

