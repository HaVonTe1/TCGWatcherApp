package de.dkutzer.tcgwatcher

import de.dkutzer.tcgwatcher.products.adapter.ProductCardmarketRepositoryAdapter
import de.dkutzer.tcgwatcher.products.config.BaseConfig
import de.dkutzer.tcgwatcher.products.config.CardmarketConfig
import io.ktor.util.date.GMTDate
import org.junit.jupiter.api.Assertions.*

class ProductCardmarketRepositoryAdapterTest {

    @org.junit.jupiter.api.Test
    fun search() {
        val cardmarketConfig = CardmarketConfig()
        val repositoryAdapter =
            ProductCardmarketRepositoryAdapter(cardmarketConfig)

        val searchItems = repositoryAdapter.search("Bisa")
        searchItems.forEach {
            println(it)
        }

    }

//    @org.junit.jupiter.api.Test
//    fun test_buildSearchRequestBody() {
//
//        val productCardmarketRepositoryAdapter = ProductCardmarketRepositoryAdapter(TestConfig())
//        val buildSearchRequestBody =
//            productCardmarketRepositoryAdapter.buildSearchRequestBody("test")
//        println(buildSearchRequestBody)
//    }

    data class TestConfig(override val searchUrl: String = "", override val baseUrl: String = "",
                          override val lang: String = "de") : BaseConfig(searchUrl, lang, baseUrl)

}