package de.dkutzer.tcgwatcher

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import de.dkutzer.tcgwatcher.models.ItemDetails
import de.dkutzer.tcgwatcher.models.ItemOfInterest
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

class Datasource() {
    fun loadMockData(): List<ItemOfInterest> {
        return MutableList(Random.nextInt(1,10)) { randomItem()}
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



    fun loadFromCardMarket() {
        skrape(BrowserFetcher) { // <-- pass any Fetcher, e.g. HttpFetcher, BrowserFetcher, ...
            request {
                // ... request options goes here, e.g the most basic would be url
                url ="https://www.cardmarket.com/de/Pokemon/Products/Singles/151/Blastoise-ex-V1-MEW009?language=3"
            }
            response {
                println(responseBody)
                status { code }
            }
        }

    }


}

