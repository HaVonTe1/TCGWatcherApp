package de.dkutzer.tcgwatcher

import de.dkutzer.tcgwatcher.products.adapter.ProductCardmarketRepositoryAdapter
import de.dkutzer.tcgwatcher.products.adapter.api.CardmarketHtmlUnitApiClientImpl
import de.dkutzer.tcgwatcher.products.config.BaseConfig
import de.dkutzer.tcgwatcher.products.config.CardmarketConfig
import org.junit.Test



class ProductCardmarketRepositoryAdapterTest {


//    @Test
//    fun search() {
//
//        val client  = CardmarketHtmlUnitApiClientImpl(CardmarketConfig())
//        val repositoryAdapter =
//            ProductCardmarketRepositoryAdapter(client)
//
//        val searchResults = repositoryAdapter.search("Glu")
//        searchResults.items.forEach {
//            println(it)
//        }
//
//    }

//    @Test
//    fun `Test get ImageUrl from DetailsPage`() {
//        val client  = CardmarketHtmlUnitApiClientImpl(CardmarketConfig())
//        val repositoryAdapter =
//            ProductCardmarketRepositoryAdapter(client)
//
//        val imageUrlById = repositoryAdapter.getProductDetails("https://www.cardmarket.com/de/Pokemon/Products/Singles/151/Vulpix-MEW037")
//
//
////        assertEquals("dsfsd", imageUrlById)
//
//    }


    @Test
    fun testRegex1() {
        val t1 = " Seite 1 von 48 "
        val paginationRegex = "\\b(?:von|of|de) (\\d+)\\b".toRegex()

        val find = paginationRegex.find(t1)
        println(find)
        println(find?.groupValues?.getOrNull(1))
    }
    data class TestConfig(override val searchUrl: String = "", override val baseUrl: String = "",
                          override val lang: String = "de") : BaseConfig(searchUrl, lang, baseUrl)

}

