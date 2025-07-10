package de.dkutzer.tcgwatcher.collectables.history.data

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductComposite
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductNameEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductSetEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithBasicProductsInfo
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithFullProductInfo
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


    private fun createSampleProductComposite(
        code: String = "TEST123"
    ): ProductComposite {
        return ProductComposite(
            productEntity = ProductEntity(
                id = 0,
                language = "en",
                genre = "Pokemon",
                type = "Singles",
                rarity = "Rare",
                code = code,
                externalId = "test-id",
                externalLink = "/en/Pokemon/Products/Singles/test/123",
                imgLink = "http://example.com/image.jpg",
                price = "25.00",
                priceTrend = "+10%",
                lastUpdated = System.currentTimeMillis()
            ),
            names = listOf(
                ProductNameEntity(id = 0, productId = 0, name = "Test Product", language = "en"),
                ProductNameEntity(id = 0, productId = 0, name = "Test Produkt", language = "de")
            ),
            set = ProductSetEntity(
                id = 0,
                productId = 0,
                setName = "Test Set",
                setId = "set-123",
                language = "en"
            )
        )
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
        val retrieved = dao.getSearchByTerm(searchTerm)

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
            createSampleProductComposite(),
            createSampleProductComposite(),
        )

        val searchWithBasicProductsInfo = SearchWithBasicProductsInfo(search, products)

        // Persist
        val persisted = repository.persistSearchWithProducts(searchWithBasicProductsInfo, "en")

        // Verify relationship
        val retrieved = repository.getSearchWithBasicProductsByQuery(searchTerm, 1, 10)

        assertNotNull(retrieved)
        assertEquals(2, retrieved?.products?.size ?: 0)
        assertEquals(searchTerm, retrieved?.search?.searchTerm)
    }


    @Test
    fun testPagination() = runBlocking {
        val searchTerm = "PagedSearch"
        val products = (1..20).map {
            createSampleProductComposite(code = "PGD-$it")
        }

        repository.persistSearchWithProducts(
            SearchWithBasicProductsInfo(
                SearchEntity(searchTerm = searchTerm, size = 20, language =  "en", lastUpdated =  System.currentTimeMillis(), history = true),
                products
            ),
            "en"
        )

        // Page 1
        val page1 = repository.getSearchWithBasicProductsByQuery(searchTerm, 1, 5)
        assertEquals(5, page1?.products?.size)
        assertEquals("PGD-1", page1?.products?.get(0)?.productEntity?.code)

        // Page 2
        val page2 = repository.getSearchWithBasicProductsByQuery(searchTerm, 2, 5)
        assertEquals(5, page2?.products?.size)
        assertEquals("PGD-6", page2?.products?.get(0)?.productEntity?.code)
    }

    @Test
    fun testProductWithOffers() = runBlocking {
        val productWithSellOffers = ProductWithSellOffers(
            productEntity = createSampleProductComposite().productEntity,
            offers = listOf(
                createSampleSellOfferEntity(price = "10.99"),
                createSampleSellOfferEntity(price = "9.50")
            ),
            names = createSampleProductComposite().names,
            set = createSampleProductComposite().set
        )

        val searchWithOffers = SearchWithFullProductInfo(
            search = SearchEntity(
                searchTerm = "OffersTest",
                size = 1,
                language = "en",
                lastUpdated = System.currentTimeMillis(),
                history = true
            ),
            fullProducts = listOf(productWithSellOffers)
        )

        val persisted = repository.persistSearchWithProducts(searchWithOffers, "en")

        // Verify offers were persisted
        val productId = productWithSellOffers.productEntity.id
        val offers = dao.getSellOffersByProductId(productId)
        assertEquals(2, offers.size)
        assertTrue(offers.all { it.productId == productId })
    }

    @Test
    fun testUpdateByLink(): Unit = runBlocking {
        val link = "special-card-link"
        val initialItem = ProductEntity(
            externalLink = link,
            price = "10.00",
            lastUpdated = 0,
            id = 0,
            language = "de",
            genre = GenreType.POKEMON.cmCode,
            type = TypeEnum.CARD.cmCode,
            rarity = RarityType.UNCOMMON.cmCode,
            code = "sdf",
            imgLink = "sdf",
            priceTrend = "sdfg",
            externalId = "sdf"
        )
        repository.persistProducts(listOf(initialItem.copy(id = 1)))
        // Name und Set anlegen
        val nameEntity = ProductNameEntity(
            productId = 1,
            language = "de",
            name = "Testkarte"
        )
        val setEntity = ProductSetEntity(
            productId = 1,
            setName = "TestSet",
            setId = "set-123",
            language = "de"
        )
        // Update
        val updatedItem = initialItem.copy(
            price = "15.00",
            lastUpdated = System.currentTimeMillis(),
            id = 1
        )
        repository.updateProductByDetailsUrl(link, updatedItem, names = listOf(nameEntity), set = setEntity)
        // Verify Product
        val productsByExternalId = repository.getProductsByExternalId(initialItem.externalId)
        assertNotNull(productsByExternalId)
        productsByExternalId?.let {
            // Verify Names
            assertEquals(1, it.names.size)
            val actualName = it.names[0]
            assertEquals(nameEntity.name, actualName.name)
            assertEquals(nameEntity.language, actualName.language)
            assertEquals(nameEntity.productId, actualName.productId)
            // Verify Sets
            assertEquals(setEntity.setName, it.set?.setName)
            assertEquals(setEntity.setId, it.set?.setId)
            assertEquals(setEntity.language, it.set?.language)
            assertEquals(setEntity.productId, it.set?.productId)
        }
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
        val product =
            createSampleProductComposite(code = "ORPH-1").productEntity.copy(id = 1)

        dao.upsertSearch(search)
        repository.persistProducts(listOf(product))

        repository.deleteSearch(search)

        val remainingProducts = dao.getProductsBySearchId(1, 10, 0)
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
            val composite = createSampleProductComposite(code = "PGD-$it")
            ProductWithSellOffers(
                productEntity = composite.productEntity,
                offers = listOf(createSampleSellOfferEntity(), createSampleSellOfferEntity()),
                names = composite.names,
                set = composite.set
            )
        }
        val searchWithFullProducts =
            SearchWithFullProductInfo(
                search = SearchEntity(
                    searchTerm = searchTerm,
                    size = 20,
                    language = "en",
                    lastUpdated = System.currentTimeMillis(),
                    history = true
                ),
                fullProducts = productWithSellOffers
            )
        val persisted =
            repository.persistSearchWithProducts(
                searchWithFullProducts,
                "en"
            )
        val pagingSource = dao.getProductWithSellOffersPagingSource(searchTerm)
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
        val productItemEntity = productWithSellOffers[0].productEntity
        val productId = productItemEntity.id
        assertEquals(productId, product.productEntity.id)

        // Verify offers
        assertEquals(2, product.offers.size)
        assertEquals("sdfsdfsdf", product.offers[0].sellerName)
        assertEquals("10.99", product.offers[0].price)
    }

    companion object TestHelpers {
        fun createSampleProductItemEntity(code : String = "DRG-1"): ProductEntity =
            ProductEntity(
                id = 0,
                language = "de",
                genre = GenreType.POKEMON.cmCode,
                type = TypeEnum.CARD.cmCode,
                rarity = RarityType.UNCOMMON.cmCode,
                code = code,
                externalId = "blub",
                externalLink = "/de/pokemon/Products/Singles/bla/blub",
                imgLink = "https://fddgf.dsdfdfg.jpg",
                price = "10.00",
                priceTrend = "11.00",
                lastUpdated = 2111111111111111111
            )

        fun createSampleSellOfferEntity(price : String = "10.99", productId : Int = 0): SellOfferEntity = SellOfferEntity(
            id = 0,
            productId = productId,
            sellerName = "sdfsdfsdf",
            sellerLocation = "Deutschlang",
            productLanguage = "Deutsch",
            condition = ConditionType.NEAR_MINT.cmCode,
            amount = 1,
            price = price,
            special = SpecialType.REVERSED.cmCode
        )

        fun createSampleProductComposite(
            code: String = "DRG-1",
        ): ProductComposite =
            ProductComposite(
                productEntity = ProductEntity(
                    id = 0,
                    language = "de",
                    genre = GenreType.POKEMON.cmCode,
                    type = TypeEnum.CARD.cmCode,
                    rarity = RarityType.UNCOMMON.cmCode,
                    code = code,
                    externalId = "blub",
                    externalLink = "/de/pokemon/Products/Singles/bla/blub",
                    imgLink = "https://fddgf.dsdfdfg.jpg",
                    price = "10.00",
                    priceTrend = "11.00",
                    lastUpdated = 2111111111111111111
                ),
                names = listOf(
                    ProductNameEntity(
                        id = 0,
                        productId = 0,
                        name = "Test Product",
                        language = "de"
                    ),
                    ProductNameEntity(id = 0, productId = 0, name = "Test Produkt", language = "en")
                ),
                set = ProductSetEntity(
                    id = 0,
                    productId = 0,
                    setName = "Test Set",
                    setId = "set-123",
                    language = "de"
                )
            )
    }
}