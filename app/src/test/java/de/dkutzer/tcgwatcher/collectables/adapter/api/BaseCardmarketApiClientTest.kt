package de.dkutzer.tcgwatcher.collectables.adapter.api

import de.dkutzer.tcgwatcher.TestApiClient
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test

class BaseCardmarketApiClientTest {


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