package de.dkutzer.tcgwatcher

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import de.dkutzer.tcgwatcher.models.ItemDetails
import de.dkutzer.tcgwatcher.models.ItemOfInterest
import de.dkutzer.tcgwatcher.products.adapter.ProductCardmarketRepositoryAdapter
import de.dkutzer.tcgwatcher.products.adapter.api.CardmarketApiClientImpl
import de.dkutzer.tcgwatcher.products.config.CardmarketConfig
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.div
import it.skrape.selects.text
import kotlinx.coroutines.flow.flowOf
import java.time.OffsetDateTime
import java.util.Locale
import kotlin.random.Random

class Datasource {
    fun loadMockData(): MutableList<ItemOfInterest> {
        return MutableList(Random.nextInt(1,10)) { randomItem()}
    }

    fun loadRealTestData() : MutableList<ItemOfInterest> {
        val repo = ProductCardmarketRepositoryAdapter(CardmarketApiClientImpl(CardmarketConfig()))
        val searchItems = repo.search("Bisaflor")

        val itemOfInterests = searchItems.map {
            ItemOfInterest(
                id = Random.nextLong(),
                stringResourceId = R.string.bisaflor,
                imageResourceId = R.drawable.bisaflor,
                details = ItemDetails(
                    name = it.displayName,
                    price = it.price,
                    lastUpdate = OffsetDateTime.now()
                )
            )
        }.toMutableList()
        return itemOfInterests

    }

    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun randomStringByKotlinRandom() = List(10) { charPool.random() }.joinToString("")
    private fun randomItem() : ItemOfInterest {
        return ItemOfInterest(
            Random.nextLong(),
            R.string.bisaflor,
            R.drawable.bisaflor,
            ItemDetails(
                randomStringByKotlinRandom(), CurrencyAmount(
                    Random.nextFloat(), Currency.getInstance(
                        Locale.GERMANY
                    )
                ),
                OffsetDateTime.now()
            )
        )
    }


}

