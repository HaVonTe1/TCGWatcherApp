package de.dkutzer.tcgwatcher

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import de.dkutzer.tcgwatcher.products.domain.model.ProductDetailsModel
import de.dkutzer.tcgwatcher.products.domain.model.ProductModel
import java.time.OffsetDateTime
import java.util.Locale
import kotlin.random.Random

class Datasource {
    fun loadMockData(): MutableList<ProductModel> {
        return MutableList(Random.nextInt(3,10)) { randomItem()}
    }

//    fun loadRealTestData(searchString: String) : MutableList<ProductModel> {
//        val config = CardmarketConfig()
//        val repo = ProductCardmarketRepositoryAdapter(CardmarketApiClientImpl(config))
//        val searchItems = repo.search(searchString)
//
//        val productModels = searchItems.map {
//            ProductModel(
//                id = Random.nextLong(),
//                imageUrl = "", //TODO
//                detailsUrl = "${config.baseUrl}${it.cmLink}",
//                details = ProductDetails(
//                    localName = it.displayName,
//                    intName = it.orgName,
//                    price = it.price,
//                    lastUpdate = OffsetDateTime.now()
//                )
//            )
//        }.toMutableList()
//        return productModels
//
//    }

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun randomStringByKotlinRandom() = List(10) { charPool.random() }.joinToString("")
    private fun randomItem(): ProductModel {
        return ProductModel(
            id = "Blastoise-ex-V1-MEW009",
            imageUrl = "https://product-images.s3.cardmarket.com/51/MEW/733633/733633.jpg",
            detailsUrl = "https://www.cardmarket.com/de/Pokemon/Products/Singles/151/Blastoise-ex-V1-MEW009",
            details =
            ProductDetailsModel(
                localName = randomStringByKotlinRandom(),
                intName = randomStringByKotlinRandom(),
                price = CurrencyAmount(
                    Random.nextFloat(), Currency.getInstance(
                        Locale.GERMANY
                    )
                ),
                lastUpdate = OffsetDateTime.now()
            )
        )
    }


}

