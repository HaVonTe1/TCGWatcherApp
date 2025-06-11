package de.dkutzer.tcgwatcher.collectables.adapter.api

import de.dkutzer.tcgwatcher.collectables.search.data.cardmarket.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CodeType
import de.dkutzer.tcgwatcher.collectables.search.domain.NameDto
import de.dkutzer.tcgwatcher.collectables.search.domain.PriceTrendType
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SetDto
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class BaseCardmarketApiClientTest {

    class TestApiClient : BaseCardmarketApiClient() {
        override suspend fun search(searchString: String, page: Int): SearchResultsPageDto {
            return SearchResultsPageDto(results = listOf(), page = 9609, totalPages = 4163)
        }

        override suspend fun getProductDetails(link: String): CardmarketProductDetailsDto {
            return CardmarketProductDetailsDto(
                imageUrl = "https://duckduckgo.com/?q=finibus",
                price = "interdum",
                priceTrend = PriceTrendType("0.00", false),
                detailsUrl = "https://duckduckgo.com/?q=interdum",
                name = NameDto("xxx","de","yy"),
                set = SetDto("xxx","de"),
                genre = "xxx",
                type = "xxx",
                rarity = "xxx",
                cmId = "sdfsfd",
                code = CodeType("xxx", false),
            )
        }
    }

    private val apiClientMock = TestApiClient()


    @Test
    fun parseGallerySearchResults() {

        val html =
            Thread.currentThread().contextClassLoader?.getResource("giflor_gallary.html")?.readText()
        val document = Jsoup.parse(html!!)

        val searchResults = apiClientMock.parseGallerySearchResults(document, 1)

        assertEquals(29, searchResults.results.size)

    }


}