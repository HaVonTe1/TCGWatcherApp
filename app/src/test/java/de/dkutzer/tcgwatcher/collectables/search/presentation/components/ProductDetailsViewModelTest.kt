package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.dkutzer.tcgwatcher.collectables.history.data.SearchCacheDao
import de.dkutzer.tcgwatcher.collectables.history.data.SearchCacheDatabase
import de.dkutzer.tcgwatcher.collectables.history.data.SearchCacheRepositoryImpl
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType
import de.dkutzer.tcgwatcher.collectables.search.domain.NameModel
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.RarityType
import de.dkutzer.tcgwatcher.collectables.search.domain.SetModel
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum
import de.dkutzer.tcgwatcher.settings.data.SettingsDao
import de.dkutzer.tcgwatcher.settings.data.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.data.SettingsEntity
import de.dkutzer.tcgwatcher.settings.data.SettingsRepositoryImpl
import de.dkutzer.tcgwatcher.settings.domain.Engines
import de.dkutzer.tcgwatcher.settings.domain.Languages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
class MainDispatcherRule(
    testDispatcher: TestCoroutineScheduler = TestCoroutineScheduler()
) : TestWatcher() {
    val standardTestDispatcher = StandardTestDispatcher(testDispatcher)
    override fun starting(description: Description) {
        Dispatchers.setMain(standardTestDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ProductDetailsViewModelTest {

    // Add test coroutine dispatcher
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Rule to replace the main dispatcher
    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher.scheduler)

    private lateinit var searchCacheDatabase: SearchCacheDatabase
    private lateinit var searchCacheDao: SearchCacheDao
    private lateinit var searchCacheRepository: SearchCacheRepositoryImpl

    private lateinit var settingsDatabase: SettingsDatabase
    private lateinit var settingsDao: SettingsDao
    private lateinit var settingsRepository: SettingsRepositoryImpl


    @Before
    fun setup() {
        ShadowLog.stream = System.out
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Initialize Robolectric
        RuntimeEnvironment.application = context.applicationContext as Application

        searchCacheDatabase = Room.inMemoryDatabaseBuilder(
            context,
            SearchCacheDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        searchCacheDao = searchCacheDatabase.searchCacheDao
        searchCacheRepository = SearchCacheRepositoryImpl(searchCacheDao)

        settingsDatabase = Room.inMemoryDatabaseBuilder(
            context,
            SettingsDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        settingsDao = settingsDatabase.settingsDao
        settingsRepository = SettingsRepositoryImpl(settingsDao)


    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun teardown() {
        Dispatchers.resetMain()
        searchCacheDatabase.close()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Test onReloadSingleItem DE`() = testScope.runTest {

        runBlocking {
            SettingsRepositoryImpl(settingsDatabase.settingsDao).save(
                SettingsEntity(1, Languages.DE, Engines.TESTING)
            )
            val searchEntity = SearchEntity(
                id = 1,
                searchTerm = "evoli",
                size = 1,
                language = "de",
                lastUpdated = System.currentTimeMillis(),
                history = true
            )
            searchCacheRepository.persistSearch(searchEntity)
            val searchItems = listOf(
                ProductEntity(
                    id = 1,
                    searchId = 1,
                    displayName = "Evoli",
                    language = "de",
                    genre = "Pokemon",
                    type = "Card",
                    rarity = "Other",
                    code = "",
                    externalId = "/Pokemon/Products/Singles/Pokemon-Jungle/Eevee",
                    externalLink = "/de/Pokemon/Products/Singles/Pokemon-Jungle/Eevee",
                    imgLink = "https://product-images.s3.cardmarket.com/51/PJU/584686/584686.jpg",
                    price = "0.02",
                    priceTrend = "",
                    setName = "Pokemon-Jungle",
                    setId = "Pokemon-Jungle",
                    lastUpdated = System.currentTimeMillis()
                )
            )
            searchCacheRepository.persistProducts(searchItems)
        }

        val viewModel = ProductDetailsViewModel(
            searchCacheDatabase,
            settingsDatabase,
            testDispatcher
        )
        advanceUntilIdle()

        val productModel = ProductModel(
            id = "/Pokemon/Products/Singles/Pokemon-Jungle/Eevee",
            name = NameModel("Eevee", "de"),
            type = TypeEnum.CARD,
            genre = GenreType.POKEMON,
            code = "",
            externalId = "/Pokemon/Products/Singles/Pokemon-Jungle/Eevee",
            imageUrl = "https://product-images.s3.cardmarket.com/51/PJU/584686/584686.jpg",
            detailsUrl = "/de/Pokemon/Products/Singles/Pokemon-Jungle/Eevee",
            rarity = RarityType.OTHER,
            set = SetModel("/de/Pokemon/Expansions/Pokemon-Jungle", "Pokémon Jungle"),
            price = "0.02",
            priceTrend = "",
            sellOffers = emptyList(),
            timestamp = System.currentTimeMillis()
        )

        viewModel.onLoadSingleItem(productModel, false)
        advanceUntilIdle()
        assertEquals("PJU", viewModel.reloadedSingleItem.productModel.code)
        assertEquals(RarityType.COMMON, viewModel.reloadedSingleItem.productModel.rarity)
        assertEquals("4,58 €", viewModel.reloadedSingleItem.productModel.priceTrend)
        assertEquals(50, viewModel.reloadedSingleItem.productModel.sellOffers.size)

        val seller1 = viewModel.reloadedSingleItem.productModel.sellOffers[0]
        assertEquals("GeCaFeProject", seller1.sellerName)
        assertEquals("Italien", seller1.sellerLocation.country)
        assertEquals("it", seller1.sellerLocation.code)
        assertEquals("Japanisch", seller1.productLanguage.displayName)
        assertEquals("ja", seller1.productLanguage.code)
        assertEquals("Poor", seller1.condition.cmCode)
        assertEquals(1, seller1.amount)

        val seller2 = viewModel.reloadedSingleItem.productModel.sellOffers[1]
        assertEquals("FrlMeow", seller2.sellerName)
        assertEquals("Deutschland", seller2.sellerLocation.country)
        assertEquals("de", seller2.sellerLocation.code)
        assertEquals("Japanisch", seller2.productLanguage.displayName)
        assertEquals("ja", seller2.productLanguage.code)
        assertEquals("Poor", seller2.condition.cmCode)
        assertEquals(1, seller2.amount)

        assertEquals("/de/Pokemon/Products/Singles/Pokemon-Jungle/Eevee", viewModel.reloadedSingleItem.productModel.detailsUrl)
        assertEquals("Evoli", viewModel.reloadedSingleItem.productModel.name.value)
        assertEquals("de", viewModel.reloadedSingleItem.productModel.name.languageCode)
        assertEquals(TypeEnum.CARD, viewModel.reloadedSingleItem.productModel.type)
        assertEquals("Pokemon", viewModel.reloadedSingleItem.productModel.genre.cmCode)
        assertEquals(GenreType.POKEMON, viewModel.reloadedSingleItem.productModel.genre)
        assertEquals("https://product-images.s3.cardmarket.com/51/PJU/584686/584686.jpg", viewModel.reloadedSingleItem.productModel.imageUrl)
        assertEquals("0,10 €", viewModel.reloadedSingleItem.productModel.price)
        assertEquals("4,58 €", viewModel.reloadedSingleItem.productModel.priceTrend)
        assertNotNull(viewModel.reloadedSingleItem.productModel.timestamp)

        val fromDB = searchCacheRepository.getFullProductInfoByExternalId(productModel.externalId)
        println(fromDB)
        assertNotNull(fromDB)
        assertEquals(50, fromDB.offers.size)
        assertEquals("4,58 €", fromDB.productEntity.priceTrend)
        assertEquals(RarityType.COMMON.cmCode, fromDB.productEntity.rarity)
        assertEquals("PJU", fromDB.productEntity.code)
        assertEquals("Evoli", fromDB.productEntity.displayName)
        assertEquals("de", fromDB.productEntity.language)
        assertEquals("/de/Pokemon/Products/Singles/Pokemon-Jungle/Eevee", fromDB.productEntity.externalLink)
        assertEquals("https://product-images.s3.cardmarket.com/51/PJU/584686/584686.jpg", fromDB.productEntity.imgLink)
        assertEquals("Pokemon", fromDB.productEntity.genre)
        assertEquals("Pokémon Jungle", fromDB.productEntity.setName)
        assertEquals("/de/Pokemon/Expansions/Pokemon-Jungle", fromDB.productEntity.setId)
        assertEquals("Singles", fromDB.productEntity.type)
        assertNotNull(fromDB.productEntity.lastUpdated)


        val sellOfferEntity1 = fromDB.offers[0]
        assertEquals("GeCaFeProject", sellOfferEntity1.sellerName)
        assertEquals("it", sellOfferEntity1.sellerLocation)
        assertEquals("ja", sellOfferEntity1.productLanguage)
        assertEquals("Poor", sellOfferEntity1.condition)
        assertEquals(1, sellOfferEntity1.amount)


        val sellOfferEntity2 = fromDB.offers[1]
        assertEquals("FrlMeow", sellOfferEntity2.sellerName)
        assertEquals("de", sellOfferEntity2.sellerLocation)
        assertEquals("ja", sellOfferEntity2.productLanguage)
        assertEquals("Poor", sellOfferEntity2.condition)
        assertEquals(1, sellOfferEntity2.amount)

    }

}