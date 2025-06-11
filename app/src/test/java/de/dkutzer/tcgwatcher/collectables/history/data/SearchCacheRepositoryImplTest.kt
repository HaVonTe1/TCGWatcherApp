package de.dkutzer.tcgwatcher.collectables.history.data

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithProducts
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithProductsAndSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.SellOfferEntity
import de.dkutzer.tcgwatcher.collectables.search.domain.ConditionType
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType
import de.dkutzer.tcgwatcher.collectables.search.domain.RarityType
import de.dkutzer.tcgwatcher.collectables.search.domain.SpecialType
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SearchCacheRepositoryImplTest {
    private lateinit var database: SearchCacheDatabase
    private lateinit var dao: SearchCacheDao
    private lateinit var repository: SearchCacheRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Initialize Robolectric
        RuntimeEnvironment.application = context.applicationContext as Application

        database = Room.inMemoryDatabaseBuilder(
            context,
            SearchCacheDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.searchCacheDao
        repository = SearchCacheRepositoryImpl(dao)
    }


    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveSearch() = runBlocking {
        val searchTerm = "Black Lotus"
        val searchEntity = SearchEntity(
            searchTerm = searchTerm,
            size = 10,
            language = "en",
            lastUpdated = System.currentTimeMillis(),
            history = true
        )

        // Insert
        repository.persistSearch(searchEntity)

        // Retrieve
        val retrieved = dao.findSearch(searchTerm)

        assertNotNull(retrieved)
        assertEquals(searchTerm, retrieved?.searchTerm)
        assertEquals(10, retrieved?.size ?: 0)
    }

    @Test
    fun testSearchWithProductsRelationship() = runBlocking {
        val searchTerm = "Dragon"
        val search = SearchEntity(
            searchTerm = searchTerm,
            size = 2,
            lastUpdated = System.currentTimeMillis(),
            language = "en",
            history = true
        )

        val products = listOf(
            createSampleProductItemEntity(),
            createSampleProductItemEntity(),
        )

        val searchWithProducts = SearchWithProducts(search, products)

        // Persist
        val persisted = repository.persistsSearchWithItems(searchWithProducts, "en")

        // Verify relationship
        val retrieved = repository.findSearchWithItemsByQuery(searchTerm, 1, 10)

        assertNotNull(retrieved)
        assertEquals(2, retrieved?.products?.size ?: 0)
        assertEquals(searchTerm, retrieved?.search?.searchTerm)
        assertTrue(retrieved?.products?.all { it.searchId == persisted.search.id } == true)
    }


    @Test
    fun testPagination() = runBlocking {
        val searchTerm = "PagedSearch"
        val products = (1..20).map {
            createSampleProductItemEntity(code = "PGD-$it")
        }

        repository.persistsSearchWithItems(
            SearchWithProducts(
                SearchEntity(searchTerm = searchTerm, size = 20, language =  "en", lastUpdated =  System.currentTimeMillis(), history = true),
                products
            ),
            "en"
        )

        // Page 1
        val page1 = repository.findSearchWithItemsByQuery(searchTerm, 1, 5)
        assertEquals(5, page1?.products?.size)
        assertEquals("PGD-1", page1?.products?.get(0)?.code)

        // Page 2
        val page2 = repository.findSearchWithItemsByQuery(searchTerm, 2, 5)
        assertEquals(5, page2?.products?.size)
        assertEquals("PGD-6", page2?.products?.get(0)?.code)
    }

    @Test
    fun testProductWithOffers() = runBlocking {
        val productWithSellOffers = ProductWithSellOffers(
            createSampleProductItemEntity(code = "OFR-1"),
            listOf(
                createSampleSellOfferEntity(price = "10.99"),
                createSampleSellOfferEntity(price = "9.50")
            )
        )

        val searchWithOffers = SearchWithProductsAndSellOffers(
            SearchEntity(searchTerm = "OffersTest", size =  1, language =  "en", lastUpdated =  System.currentTimeMillis(), history = true),
            listOf(productWithSellOffers)
        )

        val persisted = repository.persistSearchWithProductAndSellOffers(searchWithOffers, "en")

        // Verify offers were persisted
        val offers = dao.findSellOfferByProductId(persisted.productWithSellOffers[0].productEntity.id)
        assertEquals(2, offers.size)
        assertTrue(offers.all { it.productId == persisted.productWithSellOffers[0].productEntity.id })
    }

    @Test
    fun testUpdateByLink() = runBlocking {
        val link = "special-card-link"
        val initialItem = ProductEntity(
            externalLink = link,
            price = "10.00",
            lastUpdated = 0,
            id = 0,
            searchId = 0,
            displayName = "sdf",
            language = "de",
            genre = GenreType.POKEMON.cmCode,
            type = TypeEnum.CARD.cmCode,
            rarity = RarityType.UNCOMMON.cmCode,
            code = "sdf",
            orgName = "sef",
            imgLink = "sdf",
            priceTrend = "sdfg",
            setName = "sdfgdg",
            externalId = "sdf",
            setId = "sdg"
        )

        repository.persistSearchItems(listOf(initialItem))

        // Update
        val updatedItem = initialItem.copy(
            price = "15.00",
            lastUpdated = System.currentTimeMillis()
        )

        repository.updateItemByLink(link, updatedItem)

        // Verify
        val items = repository.findItemsByLink(link)
        assertEquals("15.00", items[0].price)
        assertTrue(items[0].lastUpdated > 0)
    }

    @Test
    fun testSearchHistoryOrder() = runBlocking {
        val searches = listOf(
            SearchEntity(
                searchTerm = "A", lastUpdated = 1, history = true,
                size = 1,
                language = "de"
            ),
            SearchEntity(
                searchTerm = "B", lastUpdated = 3, history = true,
                size = 1,
                language = "de"
            ),
            SearchEntity(
                searchTerm = "C", lastUpdated = 2, history = false,
                size = 1,
                language = "de"
            )
        )

        searches.forEach { repository.persistSearch(it) }

        val history = repository.getSearchHistory()
        assertEquals(listOf("B", "A"), history) // Ordered by lastUpdated DESC
    }

    @Test
    fun testDeleteSearchKeepsProducts() = runBlocking {
        val search = SearchEntity(searchTerm = "Delete OrphanTest", id = 1, size = 1, language = "de", lastUpdated = System.currentTimeMillis(), history = true )
        val product = createSampleProductItemEntity( code = "ORPH-1").apply { searchId = 1 }


        dao.saveSearch(search)
        repository.persistSearchItems(listOf(product))

        repository.deleteSearch(search)

        val remainingProducts = dao.findSearchResultsBySearchId(1, 10, 0)
        assertTrue(remainingProducts.isNotEmpty())
    }

    @Test
    fun testProcessSearchCreatesNewEntry() = runBlocking {
        val searchTerm = "NewSearch"

        // First call - creates new
        val (initialEntity, initialId) = repository.processSearch(
            searchTerm = searchTerm,
            productsSize = 5,
            language = "en",
            history = true
        )

        assertTrue(initialId > 0)

        // Second call - updates existing
        val (updatedEntity, updatedId) = repository.processSearch(
            searchTerm = searchTerm,
            productsSize = 10,
            language = "en",
            history = true
        )

        assertEquals(initialId, updatedId)
        assertEquals(10, updatedEntity.size)
    }

    @Test
    fun testFindProductsWithSellOffers() = runBlocking {
        val searchTerm = "OffersTest"
        val productWithSellOffers = (1..20).map {
            val productItemEntity = createSampleProductItemEntity(code = "PGD-$it")
            ProductWithSellOffers(productItemEntity, listOf(createSampleSellOfferEntity(),createSampleSellOfferEntity()))
        }
        val persistSearchWithProductAndSellOffers =
            repository.persistSearchWithProductAndSellOffers(
                SearchWithProductsAndSellOffers(
                    SearchEntity(
                        searchTerm = searchTerm,
                        size = 20,
                        language = "en",
                        lastUpdated = System.currentTimeMillis(),
                        history = true
                    ),
                    productWithSellOffers
                ),
                "en"
            )
        val pagingSource = dao.findItemsWithSellOffersByQuery(searchTerm)
        // Load the paging data
        val loadParams = PagingSource.LoadParams<Int>.Refresh(
            key = 0,
            loadSize = 10,
            placeholdersEnabled = false
        )
        val loadResult = pagingSource.load(loadParams)

        // Verify the results
        assertTrue(loadResult is PagingSource.LoadResult.Page)
        val page = loadResult as PagingSource.LoadResult.Page<Int, ProductWithSellOffers>

        assertEquals(10, page.data.size)
        val product = page.data[0]

        // Verify product item
        val productItemEntity = persistSearchWithProductAndSellOffers.productWithSellOffers[0].productEntity
        val productId = productItemEntity.id
        assertEquals(productId, product.productEntity.id)
        assertEquals(productItemEntity.displayName, product.productEntity.displayName)

        // Verify offers
        assertEquals(2, product.offers.size)
        assertEquals("sdfsdfsdf", product.offers[0].sellerName)
        assertEquals("10.99", product.offers[0].price)
    }

    private fun createSampleProductItemEntity(code : String = "DRG-1"): ProductEntity =
        ProductEntity(
            searchId = 0, code = code,
            id = 0,
            displayName = "sdfsdf",
            language = "de",
            genre = GenreType.POKEMON.cmCode,
            type = TypeEnum.CARD.cmCode,
            rarity = RarityType.UNCOMMON.cmCode,
            orgName = "sdfsf",
            externalLink = "/de/pokemon/Products/Singles/bla/blub",
            imgLink = "https://fddgf.dsdfdfg.jpg",
            price = "10.00",
            priceTrend = "11.00",
            setName = "bla",
            setId = "/de/Pokemon/Products/bla",
            externalId = "blub",
            lastUpdated = 2111111111111111111
        )

    private fun createSampleSellOfferEntity(price : String = "10.99", productId : Int = 0): SellOfferEntity = SellOfferEntity(
        id = 0,
        productId = 0,
        sellerName = "sdfsdfsdf",
        sellerLocation = "Deutschlang",
        productLanguage = "Deutsch",
        condition = ConditionType.NEAR_MINT.cmCode,
        amount = 1,
        price = price,
        special = SpecialType.REVERSED.cmCode
    )
}