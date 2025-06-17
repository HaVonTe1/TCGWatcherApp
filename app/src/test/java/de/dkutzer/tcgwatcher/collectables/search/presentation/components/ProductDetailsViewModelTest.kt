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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ProductDetailsViewModelTest {

    private lateinit var searchCacheDatabase: SearchCacheDatabase
    private lateinit var searchCacheDao: SearchCacheDao
    private lateinit var searchCacheRepository: SearchCacheRepositoryImpl

    private lateinit var settingsDatabase: SettingsDatabase
    private lateinit var settingsDao: SettingsDao
    private lateinit var settingsRepository: SettingsRepositoryImpl


    @Before
    fun setup() {
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


    @After
    fun teardown() {
        searchCacheDatabase.close()
    }


    @Test
    fun onLoadSingleItem() {

        runBlocking {
           SettingsRepositoryImpl(settingsDatabase.settingsDao).save(
                SettingsEntity(1, Languages.DE, Engines.TESTING)
            )
        }

        val viewModel = ProductDetailsViewModel(searchCacheDatabase, settingsDatabase)

        val productModel = ProductModel(
            id = "/Pokemon/Products/Singles/Pokemon-Jungle/Eevee",
            name = NameModel("Eevee", "de", "Eevee"),
            type = TypeEnum.CARD,
            genre = GenreType.POKEMON,
            code = "ka",
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
    }

}