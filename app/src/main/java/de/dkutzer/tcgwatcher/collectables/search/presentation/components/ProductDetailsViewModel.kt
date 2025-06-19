package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.dkutzer.tcgwatcher.collectables.history.data.SearchCacheDatabase
import de.dkutzer.tcgwatcher.collectables.history.data.SearchCacheRepositoryImpl
import de.dkutzer.tcgwatcher.collectables.search.data.ApiClientFactory
import de.dkutzer.tcgwatcher.collectables.search.data.ProductsSearchServiceFactory
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType
import de.dkutzer.tcgwatcher.collectables.search.domain.NameModel
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductSearchService
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductsApiClient
import de.dkutzer.tcgwatcher.collectables.search.domain.RarityType
import de.dkutzer.tcgwatcher.collectables.search.domain.RefreshState
import de.dkutzer.tcgwatcher.collectables.search.domain.SetModel
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum
import de.dkutzer.tcgwatcher.collectables.search.presentation.SearchModelCreationKeys
import de.dkutzer.tcgwatcher.collectables.search.presentation.SingleItemReloadState
import de.dkutzer.tcgwatcher.settings.data.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.data.SettingsRepositoryImpl
import de.dkutzer.tcgwatcher.settings.data.toModel
import de.dkutzer.tcgwatcher.settings.domain.BaseConfig
import de.dkutzer.tcgwatcher.settings.domain.ConfigFactory
import de.dkutzer.tcgwatcher.settings.domain.SettingsModel
import de.dkutzer.tcgwatcher.settings.presentation.SettingModelCreationKeys
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

class ProductDetailsViewModel(
    private val searchCacheDatabase: SearchCacheDatabase,
    private val settingsDatabase: SettingsDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO // Add this
    ): ViewModel() {


    // Define ViewModel factory in a companion object
    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                logger.debug { "Creating ProductDetailsViewModel" }
                val settingsDb = extras[SettingModelCreationKeys.SettingsDbIdKey]
                val searchCacheDb = extras[SearchModelCreationKeys.SearchCacheRepoIdKey]

                return ProductDetailsViewModel(
                    searchCacheDatabase = searchCacheDb!!,
                    settingsDatabase = settingsDb!!,

                ) as T
            }
        }
    }

    var settings by mutableStateOf(SettingsModel())
        private set


    init {
        logger.debug { "ProductDetailsViewModel::init" }
        viewModelScope.launch(ioDispatcher) {
            val settingsEntity = SettingsRepositoryImpl(settingsDatabase.settingsDao).load()
            logger.debug { "SettingsEntity: $settingsEntity" }
            settings = settingsEntity.toModel()
        }
    }

    private val apiConfig : BaseConfig by lazy { ConfigFactory(settingsModel = settings).create() }
    private val productsApiClient: ProductsApiClient by lazy { ApiClientFactory(apiConfig).create() }
    private val searchCacheRepository = SearchCacheRepositoryImpl(searchCacheDatabase.searchCacheDao)

    private val productSearchService: ProductSearchService by lazy {
        ProductsSearchServiceFactory(productsApiClient, searchCacheRepository, apiConfig).create()
    }

    var reloadedSingleItem by     mutableStateOf(SingleItemReloadState(RefreshState.IDLE,
        ProductModel.empty()))
        private set

    fun onLoadSingleItem(productModel: ProductModel, cacheOnly: Boolean)  {
        logger.debug { "ProductDetailsViewModel::onLoadSingleItem for $productModel with cacheOnly: $cacheOnly" }

        viewModelScope.launch(ioDispatcher) {
            val result = productSearchService.refreshProduct(productModel, cacheOnly)
            reloadedSingleItem = SingleItemReloadState(RefreshState.IDLE, result)
            logger.debug { "ProductDetailsViewModel::onLoadSingleItem: $reloadedSingleItem" }
        }

    }
    fun resetToProduct(productModel: ProductModel) {
        reloadedSingleItem = SingleItemReloadState(RefreshState.IDLE, productModel)
    }
}

fun ProductModel.Companion.empty(): ProductModel = ProductModel(
    id = 1.toString(),
    name = NameModel(value = "", languageCode = "", i18n = ""),
    genre = GenreType.POKEMON,
    code = "",
    imageUrl = "",
    detailsUrl = "",
    rarity = RarityType.OTHER,
    set = SetModel(link = "", name = ""),
    price = "",
    priceTrend = "",
    sellOffers = emptyList(),
    timestamp = System.currentTimeMillis(),
    type = TypeEnum.CARD,
    externalId = "bla_blub"
)
