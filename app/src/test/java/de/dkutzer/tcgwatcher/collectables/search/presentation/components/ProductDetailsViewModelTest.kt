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
    val testDispatcher: TestCoroutineScheduler = TestCoroutineScheduler()
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
    fun onLoadSingleItem() = testScope.runTest {

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
                    orgName = "Evee",
                    externalLink = "/de/Pokemon/Products/Singles/Pokemon-Jungle/Eevee",
                    imgLink = "https://product-images.s3.cardmarket.com/51/PJU/584686/584686.jpg",
                    price = "0.02",
                    priceTrend = "",
                    setName = "Pokemon-Jungle",
                    setId = "Pokemon-Jungle",
                    lastUpdated = System.currentTimeMillis()
                )
            )
            searchCacheRepository.persistSearchItems(searchItems)
        }

        val viewModel = ProductDetailsViewModel(
            searchCacheDatabase,
            settingsDatabase,
            testDispatcher
        )
        advanceUntilIdle()

        val productModel = ProductModel(
            id = "/Pokemon/Products/Singles/Pokemon-Jungle/Eevee",
            name = NameModel("Eevee", "de", "Eevee"),
            type = TypeEnum.CARD,
            genre = GenreType.POKEMON,
            code = "",
            externalId = "/Pokemon/Products/Singles/Pokemon-Jungle/Eevee",
            imageUrl = "https://product-images.s3.cardmarket.com/51/PJU/584686/584686.jpg",
            detailsUrl = "/de/Pokemon/Products/Singles/Pokemon-Jungle/Eevee",
            rarity = RarityType.OTHER,
            set = SetModel("Pokemon-Jungle", "Pokemon-Jungle"),
            price = "0.02",
            priceTrend = "",
            sellOffers = emptyList(),
            timestamp = System.currentTimeMillis()
        )

        viewModel.onLoadSingleItem(productModel, false)
        advanceUntilIdle()
        assertEquals("PJU", viewModel.reloadedSingleItem.item.code)
        assertEquals(RarityType.COMMON, viewModel.reloadedSingleItem.item.rarity)
        assertEquals("4,58 €", viewModel.reloadedSingleItem.item.priceTrend)
        assertEquals(50, viewModel.reloadedSingleItem.item.sellOffers.size)

        val fromDB = searchCacheRepository.findProductWithSellOffersByExternalId(productModel.externalId)
        println(fromDB)
        assertNotNull(fromDB)
        assertEquals(50, fromDB.offers.size)
        assertEquals("4,58 €", fromDB.productEntity.priceTrend)
        assertEquals(RarityType.COMMON.cmCode, fromDB.productEntity.rarity)
        assertEquals("PJU", fromDB.productEntity.code)


    }

}