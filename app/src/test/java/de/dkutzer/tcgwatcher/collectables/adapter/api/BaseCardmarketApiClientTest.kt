package de.dkutzer.tcgwatcher.collectables.adapter.api

import de.dkutzer.tcgwatcher.collectables.search.data.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.collectables.search.domain.CardDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class BaseCardmarketApiClientTest {

    class TestApiClient : BaseCardmarketApiClient() {
        override suspend fun search(searchString: String, page: Int): SearchResultsPageDto {
            return SearchResultsPageDto(results = listOf(), page = 9609, totalPages = 4163)
        }

        override suspend fun getProductDetails(link: String): CardDetailsDto {
            return CardDetailsDto(
                imageUrl = "https://duckduckgo.com/?q=finibus",
                price = "interdum",
                priceTrend = "aptent",
                detailsUrl = "https://duckduckgo.com/?q=interdum",
                orgName = "dfsdfsdf",
                displayName = "sfsdf",
                code = "TST 1",
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