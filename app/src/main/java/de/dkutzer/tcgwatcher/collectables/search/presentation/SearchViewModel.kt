package de.dkutzer.tcgwatcher.collectables.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.PagingData
import androidx.paging.cachedIn
import de.dkutzer.tcgwatcher.collectables.history.data.PokemonPager
import de.dkutzer.tcgwatcher.collectables.history.data.SearchCacheDatabase
import de.dkutzer.tcgwatcher.collectables.history.data.SearchCacheRepositoryImpl
import de.dkutzer.tcgwatcher.collectables.quicksearch.data.QuickSearchDatabase
import de.dkutzer.tcgwatcher.collectables.quicksearch.data.QuickSearchRepositoryImpl
import de.dkutzer.tcgwatcher.collectables.search.data.cardmarket.ApiClientFactory
import de.dkutzer.tcgwatcher.collectables.search.data.cardmarket.CardmarketPokemonRepositoryAdapter
import de.dkutzer.tcgwatcher.collectables.search.data.ProductsSearchServiceFactory
import de.dkutzer.tcgwatcher.collectables.search.data.cardmarket.GetPokemonList
import de.dkutzer.tcgwatcher.collectables.search.domain.HistorySearchItem
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductSearchService
import de.dkutzer.tcgwatcher.collectables.search.domain.QuickSearchItem
import de.dkutzer.tcgwatcher.collectables.search.domain.RefreshState
import de.dkutzer.tcgwatcher.collectables.search.domain.RefreshWrapper
import de.dkutzer.tcgwatcher.settings.data.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.data.SettingsRepositoryImpl
import de.dkutzer.tcgwatcher.settings.data.toModel
import de.dkutzer.tcgwatcher.settings.domain.ConfigFactory
import de.dkutzer.tcgwatcher.settings.domain.SettingsModel
import de.dkutzer.tcgwatcher.settings.presentation.SettingModelCreationKeys
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils

private val logger = KotlinLogging.logger {}

class SearchViewModel(
    private val settingsDatabase: SettingsDatabase,
    private val searchCacheDatabase: SearchCacheDatabase,
    quickSearchDatabase: QuickSearchDatabase,

    ) : ViewModel() {


    private val quicksearchRepository =
        QuickSearchRepositoryImpl(quickSearchDatabase.quicksearchDao)
    private val searchCacheRepository = SearchCacheRepositoryImpl(searchCacheDatabase.searchCacheDao)

    private val _settings: MutableStateFlow<SettingsModel> = MutableStateFlow(SettingsModel())
    val settings: StateFlow<SettingsModel> = _settings.asStateFlow()

    private val _showHistoryContent = MutableStateFlow(false)
    val showHistoryContent = _showHistoryContent.asStateFlow()

    private val _lastQuery: MutableStateFlow<String> = MutableStateFlow("")
    private val lastQuery: StateFlow<String> = _lastQuery.asStateFlow()

    private val _query: MutableStateFlow<String> = MutableStateFlow("")
    private val query: StateFlow<String> = _query.asStateFlow()

    private val _refreshItem: MutableStateFlow<RefreshWrapper> =
        MutableStateFlow(RefreshWrapper(item = null, query = "", state = RefreshState.IDLE))
    private val refreshItem: StateFlow<RefreshWrapper> = _refreshItem.asStateFlow()

    private val _quicksearchItem: MutableStateFlow<ProductModel?> = MutableStateFlow(null)
    private val quicksearchItem: StateFlow<ProductModel?> = _quicksearchItem.asStateFlow()


    private val apiConfig = ConfigFactory(settingsModel = settings.value).create()
    private val productsApiClient: ProductsApiClient = ApiClientFactory(apiConfig).create()
    private val productSearchService: ProductSearchService = ProductsSearchServiceFactory(productsApiClient, searchCacheRepository, apiConfig).create()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemonPagingDataFlow: Flow<PagingData<ProductModel>> =
        combine(
            query,
            refreshItem,
            quicksearchItem
        ) { latestSearchQuery, latestRefreshItem, quicksearchItem ->
            logger.debug { "SearchViewModel:: Flow of query or refreshItem or quicksearchItme changed: $latestSearchQuery, $latestRefreshItem $quicksearchItem" }

            val pokemonPager =
                PokemonPager.providePokemonPager(
                    latestSearchQuery,
                    latestRefreshItem,
                    quicksearchItem,
                    searchCacheDatabase,
                    productSearchService
                )
            val pokemonRepositoryImpl = CardmarketPokemonRepositoryAdapter(pokemonPager)
            logger.debug { "getPokemonList now" }
            val getPokemonList = GetPokemonList(pokemonRepositoryImpl)
            logger.debug { "getPokemonList done" }
            val pagingDataFlow = getPokemonList().cachedIn(viewModelScope)
            pagingDataFlow
        }.flatMapLatest { it }


    private var unfilteredHistoryItems: ArrayList<String> = arrayListOf()
    private val _historyList = MutableStateFlow<List<HistorySearchItem>>(mutableListOf())
    val historyList: StateFlow<List<HistorySearchItem>> =

        query.combine(_historyList) { text, historyItems ->
            logger.debug { "history combine: $text and $historyItems" }
            historyItems
        }.stateIn(//basically convert the Flow returned from combine operator to StateFlow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),//it will allow the StateFlow survive 5 seconds before it been canceled
            initialValue = emptyList()
        )

    private val _quickSearchList = MutableStateFlow<List<QuickSearchItem>>(mutableListOf())
    val quickSearchList: StateFlow<List<QuickSearchItem>> = _quickSearchList.asStateFlow()


    init {
        logger.debug { "SearchViewModel::init" }
        viewModelScope.launch(Dispatchers.IO) {
            val settingsEntity = SettingsRepositoryImpl(settingsDatabase.settingsDao).load()
            logger.debug { "SettingsEntity: $settingsEntity" }
            _settings.value = settingsEntity.toModel()

            val searchHistory =
                SearchCacheRepositoryImpl(searchCacheDatabase.searchCacheDao).getSearchHistory()

            logger.debug { "searchHistory: $searchHistory" }
            unfilteredHistoryItems.addAll(searchHistory)
            _historyList.value = searchHistory.map { HistorySearchItem(displayName = it) }
        }
    }


    // Define ViewModel factory in a companion object
    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                logger.debug { "Creating SearchViewModel" }
                val settingsDb = extras[SettingModelCreationKeys.SettingsDbIdKey]
                val searchCacheDb = extras[SearchModelCreationKeys.SearchCacheRepoIdKey]
                val quickSearchDb = extras[SearchModelCreationKeys.QuickSearchRepoIdKey]

                return SearchViewModel(
                    settingsDb!!,
                    searchCacheDb!!,
                    quickSearchDb!!
                ) as T
            }
        }
    }


    fun onSearchQueryChange(newQuery: String) {
        logger.debug { "SearchViewModel::onSearchQueryChange: $newQuery" }

        _quicksearchItem.value=null
        _historyList.value =
            if (newQuery.isBlank()) { //return the entire list of items if not is typed
                unfilteredHistoryItems.map { HistorySearchItem(displayName = it) }
            } else {

                val filteredHistoryItems =
                    unfilteredHistoryItems.filter { historyItem ->// filter and return a list of countries based on the text the user typed
                        historyItem.uppercase().contains(newQuery.trim().uppercase())
                    }
                logger.debug { "filter result: $filteredHistoryItems" }
                filteredHistoryItems.map { HistorySearchItem(displayName = it) }
            }

        viewModelScope.launch(Dispatchers.IO) {
            _quickSearchList.value =
                if (newQuery.isBlank()) {
                    emptyList()
                } else {

                    logger.debug { "query quick search" }
                    val pokemonCardQuickEntities = quicksearchRepository.find(newQuery)
                    val result = pokemonCardQuickEntities.map {
                        QuickSearchItem(
                            id = it.id,
                            nameDe = it.nameDe,
                            nameEn = it.nameEn,
                            nameFr = it.nameFr,
                            code = it.code,
                            cmSetId = it.cmSetId,
                            cmCardId = it.cmCardId,
                            displayName = it.nameDe //make this configurable based on the language setting
                        )
                    }
                    logger.debug { "$result" }
                    result
                }
        }
    }

    fun onBack() : Boolean {
        logger.debug { "SearchViewModel::onBack: state: ${refreshItem.value.state}" }

        return if (refreshItem.value.state == RefreshState.REFRESH_ITEM) {
            _quicksearchItem.value = null;
            _refreshItem.value = RefreshWrapper(item = null, query = query.value, state = RefreshState.IDLE);
            false
        } else true
    }


    fun onRefreshSearch() {
        logger.debug { "SearchViewModel::onRefreshSearch" }
        _quicksearchItem.value = null
        _refreshItem.value =
            RefreshWrapper(item = null, query = _query.value, state = RefreshState.REFRESH_SEARCH)
    }



    fun onSearchSubmit(searchString: String) {
        logger.debug { "SearchViewModel::onSearchSubmit: $searchString" }
        _quicksearchItem.value = null
        _refreshItem.value = RefreshWrapper(item = null, query = searchString, state = RefreshState.IDLE)

        if (searchString.isEmpty()) {
            logger.debug { "Empty search" }
            return
        }
        if (unfilteredHistoryItems.none {
                it.contentEquals(
                    other = searchString,
                    ignoreCase = true
                )
            })
            unfilteredHistoryItems.add(searchString)
        _historyList.value =
            unfilteredHistoryItems.map { HistorySearchItem(displayName = it) }
        _query.value = searchString
        _lastQuery.value = searchString
        _quickSearchList.value = emptyList()
        onActiveChanged(searchString, false)
    }

    fun onActiveChanged(query: String, active: Boolean) {
        logger.debug { "SearchModel::onActiveChanged: current show history =  ${_showHistoryContent.value}" }
        logger.debug { "SearchModel::onActiveChanged: propagated active =  $active" }
        logger.debug { "SearchModel::onActiveChanged: query =  $query" }
        logger.debug { "SearchModel::onActiveChanged: lastQuery  = ${_lastQuery.value}" }
        logger.debug { "SearchModel::onActiveChanged: filteredHistoryList  = ${_historyList.value}" }
        logger.debug { "SearchModel::onActiveChanged: quichsearchItem  = ${_quicksearchItem.value}" }

        val determineShowHistory = determineShowHistory(query, active)
        logger.debug { "SearchModel::onActiveChanged: new show history =  $determineShowHistory" }
        _showHistoryContent.value = determineShowHistory

    }

    private fun determineShowHistory(query: String, active: Boolean): Boolean {
        if (query.isBlank() && active) { //reset
            _lastQuery.value = ""
        }
        if (query.isBlank() && _lastQuery.value.isBlank()) {
            return true
        }
        if (query.isBlank())
            return false
        if (_historyList.value.isEmpty())
            return false

        if (StringUtils.equals(query, lastQuery.value))
            return false

        return true
    }

    fun onQuickSearch(item: ProductModel) {
        logger.debug { "SearchViewModel::onQuickSearch: $item" }
        _quicksearchItem.value = item
        _showHistoryContent.value = false

    }


}

data class SingleItemReloadState(val state: RefreshState, val item: ProductModel)