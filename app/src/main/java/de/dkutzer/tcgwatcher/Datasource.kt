package de.dkutzer.tcgwatcher

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import de.dkutzer.tcgwatcher.models.ItemDetails
import de.dkutzer.tcgwatcher.models.ItemOfInterest
import de.dkutzer.tcgwatcher.products.adapter.ProductCardmarketRepositoryAdapter
import de.dkutzer.tcgwatcher.products.adapter.api.CardmarketApiClientImpl
import de.dkutzer.tcgwatcher.products.config.CardmarketConfig
import java.time.OffsetDateTime
import java.util.Locale
import kotlin.random.Random

class Datasource {
    fun loadMockData(): MutableList<ItemOfInterest> {
        return MutableList(Random.nextInt(3,10)) { randomItem()}
    }

    fun loadRealTestData(searchString: String) : MutableList<ItemOfInterest> {
        val config = CardmarketConfig()
        val repo = ProductCardmarketRepositoryAdapter(CardmarketApiClientImpl(config))
        val searchItems = repo.search(searchString)

        val itemOfInterests = searchItems.map {
            ItemOfInterest(
                id = Random.nextLong(),
                imageUrl = "", //TODO
                detailsUrl = "${config.baseUrl}${it.cmLink}",
                details = ItemDetails(
                    localName = it.displayName,
                    intName = it.orgName,
                    price = it.price,
                    lastUpdate = OffsetDateTime.now()
                )
            )
        }.toMutableList()
        return itemOfInterests

    }

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun randomStringByKotlinRandom() = List(10) { charPool.random() }.joinToString("")
    private fun randomItem(): ItemOfInterest {
        return ItemOfInterest(
            id = Random.nextLong(),
            imageUrl = "https://product-images.s3.cardmarket.com/51/MEW/733633/733633.jpg",
            detailsUrl = "https://www.cardmarket.com/de/Pokemon/Products/Singles/151/Blastoise-ex-V1-MEW009",
            details =
            ItemDetails(
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

