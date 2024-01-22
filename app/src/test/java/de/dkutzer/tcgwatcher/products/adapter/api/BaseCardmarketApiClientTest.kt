package de.dkutzer.tcgwatcher.products.adapter.api

import de.dkutzer.tcgwatcher.products.domain.ProductDetailsDto
import de.dkutzer.tcgwatcher.products.domain.SearchResultsPageDto
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit4.MockKRule
import org.jsoup.Jsoup
import org.junit.Assert
import org.junit.Assert.*

import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BaseCardmarketApiClientTest {

    class TestApiClient() : BaseCardmarketApiClient() {
        override suspend fun search(searchString: String, page: Int): SearchResultsPageDto {
            return SearchResultsPageDto(results = listOf(), page = 9609, totalPages = 4163)
        }

        override suspend fun getProductDetails(link: String): ProductDetailsDto {
            return ProductDetailsDto(
                imageUrl = "https://duckduckgo.com/?q=finibus",
                localPrice = "interdum",
                localPriceTrend = "aptent"
            )
        }
    }

    val apiClientMock = TestApiClient()


    @Before
    fun setUp() {
    }

    @Test
    fun parseGallerySearchResults() {

        val html =
            Thread.currentThread().contextClassLoader?.getResource("giflor_gallary.html")?.readText()
        val document = Jsoup.parse(html!!)

        val searchResults = apiClientMock.parseGallerySearchResults(document, 1)

        assertEquals(1, searchResults.results.size)

    }

    @Test
    fun parseProductDetails() {
    }
}