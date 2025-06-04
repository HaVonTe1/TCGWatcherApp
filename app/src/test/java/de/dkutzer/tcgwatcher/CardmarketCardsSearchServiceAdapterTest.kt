package de.dkutzer.tcgwatcher

import android.net.Uri
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchAndProductsEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.search.data.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.collectables.search.data.CardmarketCardsSearchService
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductGallaryItemDto
import de.dkutzer.tcgwatcher.collectables.search.domain.NameDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.OffsetDateTime


class CardmarketCardsSearchServiceAdapterTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var apiClientMock: BaseCardmarketApiClient

    @MockK(relaxUnitFun = true)
    lateinit var cacheRepoMock: SearchCacheRepository

    @Before
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks



    private fun createResultItemDto() = CardmarketProductGallaryItemDto(
        name = NameDto("xx","de","yy"),
        code = "TST 1",
        genre = "xx",
        type = "xx",
        cmLink = "https://www.cardmarket.com/de/Pokemon/jhghj/fames",
        imgLink = "instructior",
        price = "hac",
        priceTrend = "dfsgdff",
    )

    private fun createSearchResultItemEntity() = ProductItemEntity(
        id = 8848,
        searchId = 9235,
        displayName = "Cyrus Wright",
        code = "TST 1",
        orgName = "Floyd Nieves",
        cmLink = "https://www.cardmarket.com/de/Pokemon/bla/blub",
        imgLink = "molestiae",
        price = "dictum",
        priceTrend = "sfds",
        setName = "sdfsdf",
        setLink = "sdfsdf",
        language = "de",
        lastUpdated = OffsetDateTime.now().toEpochSecond()
    )

    @Test
    fun search() = runTest{

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk()
        every { Uri.parse(any()).lastPathSegment } returns ""

        coEvery { apiClientMock.search(eq("Ramalama")) }.returns(
            SearchResultsPageDto(
                page = 1, results = listOf(createResultItemDto()
                ), totalPages = 1
            )
        )


        val searchAndProductsEntity = SearchAndProductsEntity(
            search = SearchEntity(
                id = 1,
                searchTerm = "Ramalama",
                size = 1,
                history = true,
                language = "en",
                lastUpdated = OffsetDateTime.now().toEpochSecond()
            ), products = listOf(createSearchResultItemEntity())
        )
        coEvery { cacheRepoMock.persistsSearchWithItems(any(), any()) }.returns( searchAndProductsEntity)

        coEvery { cacheRepoMock.findSearchWithItemsByQuery(eq("Ramalama"), 1) }.returns(
            null
        ).andThen(
            searchAndProductsEntity
        )

        val repositoryAdapter =
            CardmarketCardsSearchService(apiClientMock, cacheRepoMock)

        val searchResults = repositoryAdapter.searchByOffset("Ramalama", offset = 0, limit = 5, language = "en")

        Assert.assertEquals(1, searchResults.items.size)


        coVerify(exactly = 2) { cacheRepoMock.findSearchWithItemsByQuery(eq("Ramalama"), eq(1))  }
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

