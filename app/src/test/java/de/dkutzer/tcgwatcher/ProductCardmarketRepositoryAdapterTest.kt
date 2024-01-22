package de.dkutzer.tcgwatcher

import de.dkutzer.tcgwatcher.products.adapter.ProductCardmarketRepositoryAdapter
import de.dkutzer.tcgwatcher.products.adapter.api.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.products.domain.*
import de.dkutzer.tcgwatcher.products.domain.port.SearchCacheRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.OffsetDateTime


class ProductCardmarketRepositoryAdapterTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var apiClientMock: BaseCardmarketApiClient

    @MockK(relaxUnitFun = true)
    lateinit var cacheRepoMock: SearchCacheRepository

    @Before
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks


    private fun createResultItemDto() = SearchResultItemDto(
        displayName = "Vincent Le",
        orgName = "Miranda Pitts",
        cmLink = "fames",
        imgLink = "instructior",
        price = "hac"

    )

    private fun createSearchResultItemEntity() = SearchResultItemEntity(
        id = 8848,
        searchId = 9235,
        displayName = "Cyrus Wright",
        orgName = "Floyd Nieves",
        cmLink = "tation",
        imgLink = "molestiae",
        price = "dictum"

    )

    @Test
    fun search() = runTest{

        coEvery { apiClientMock.search(eq("Ramalama")) }.returns(
            SearchResultsPageDto(
                page = 1, results = listOf(createResultItemDto()
                ), totalPages = 1
            )
        )

        coEvery { cacheRepoMock.findBySearchTerm(eq("Ramalama"), 1) }.returns(
            null
        ).andThen(
            SearchWithResultsEntity(
                search = SearchEntity(
                    searchId = 1,
                    searchTerm = "Ramalama",
                    size = 1,
                    lastUpdated = OffsetDateTime.now().toEpochSecond()
                ), results = listOf(createSearchResultItemEntity())
            )
        )

        val repositoryAdapter =
            ProductCardmarketRepositoryAdapter(apiClientMock, cacheRepoMock)

        val searchResults = repositoryAdapter.search("Ramalama")

        Assert.assertEquals(1, searchResults.items.size)


        coVerify(exactly = 2) { cacheRepoMock.findBySearchTerm(eq("Ramalama"), eq(1))  }
        coVerify(exactly = 1) { apiClientMock.search(eq("Ramalama"))  }
    }


    @Test
    fun testRegex1() {
        val t1 = " Seite 1 von 48 "
        val paginationRegex = "\\b(?:von|of|de) (\\d+)\\b".toRegex()

        val find = paginationRegex.find(t1)
        Assert.assertEquals("48", find?.groupValues?.get(1))

    }

}

