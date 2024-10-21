package de.dkutzer.tcgwatcher.cards.boundary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.PagingData
import androidx.paging.cachedIn
import de.dkutzer.tcgwatcher.cards.control.CardmarketPokemonRepositoryAdapter
import de.dkutzer.tcgwatcher.cards.control.GetPokemonList
import de.dkutzer.tcgwatcher.cards.control.cache.PokemonPager
import de.dkutzer.tcgwatcher.cards.control.cache.SearchCacheDatabase
import de.dkutzer.tcgwatcher.cards.control.cache.SearchCacheRepositoryImpl
import de.dkutzer.tcgwatcher.cards.control.quicksearch.QuickSearchDatabase
import de.dkutzer.tcgwatcher.cards.control.quicksearch.QuickSearchRepositoryImpl
import de.dkutzer.tcgwatcher.cards.entity.CardmarketConfig
import de.dkutzer.tcgwatcher.cards.entity.HistorySearchItem
import de.dkutzer.tcgwatcher.cards.entity.ProductModel
import de.dkutzer.tcgwatcher.cards.entity.QuickSearchItem
import de.dkutzer.tcgwatcher.cards.entity.RefreshState
import de.dkutzer.tcgwatcher.cards.entity.RefreshWrapper
import de.dkutzer.tcgwatcher.settings.control.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.control.SettingsRepositoryImpl
import de.dkutzer.tcgwatcher.settings.entity.Engines
import de.dkutzer.tcgwatcher.settings.entity.Languages
import de.dkutzer.tcgwatcher.settings.entity.QuickSearchRepoIdKey
import de.dkutzer.tcgwatcher.settings.entity.SearchCacheRepoIdKey
import de.dkutzer.tcgwatcher.settings.entity.SettingsDbIdKey
import de.dkutzer.tcgwatcher.settings.entity.SettingsEntity
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

    private val _settings: MutableStateFlow<SettingsEntity> = MutableStateFlow(
        SettingsEntity(
            id = 1,
            language = Languages.EN,
            engine = Engines.KTOR
        )
    )
    private val settings: StateFlow<SettingsEntity> = _settings.asStateFlow()

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemonPagingDataFlow: Flow<PagingData<ProductModel>> =
        combine(
            query,
            refreshItem,
            quicksearchItem
        ) { latestSearchQuery, latestRefreshItem, quicksearchItem ->
            logger.debug { "SearchViewModel:: Flow of query or refreshItem or quicksearchItme changed: $latestSearchQuery, $latestRefreshItem $quicksearchItem" }

            val config = CardmarketConfig(settings.value)
            val productApiClient = CardmarketApiClientFactory(config).create()
            val pokemonPager =
                PokemonPager.providePokemonPager(
                    latestSearchQuery,
                    latestRefreshItem,
                    quicksearchItem,
                    searchCacheDatabase,
                    productApiClient
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
            _settings.value = settingsEntity

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
                val settingsDb = extras[SettingsDbIdKey]
                val searchCacheDb = extras[SearchCacheRepoIdKey]
                val quickSearchDb = extras[QuickSearchRepoIdKey]

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
        logger.debug { "SearchViewModel::onBack: state: ${_refreshItem.value.state}" }

        _quicksearchItem.value = null
        if(_refreshItem.value.state == RefreshState.REFRESH_ITEM) {
            _refreshItem.value =
                RefreshWrapper(item = null, query = _query.value, state = RefreshState.IDLE)
            return false
        }
        return  true

    }


    fun onRefreshSearch() {
        logger.debug { "SearchViewModel::onRefreshSearch" }
        _quicksearchItem.value = null
        _refreshItem.value =
            RefreshWrapper(item = null, query = _query.value, state = RefreshState.REFRESH_SEARCH)
    }

    fun onRefreshSingleItem(item: ProductModel) {
        logger.debug { "SearchViewModel::onRefreshSingleItem: $item" }
        _quicksearchItem.value = null
        _refreshItem.value = RefreshWrapper(item, query = "", state = RefreshState.REFRESH_ITEM)
    }

    fun onSearchSubmit(searchString: String) {
        logger.debug { "SearchViewModel::onSearchSubmit: $searchString" }
        _quicksearchItem.value = null
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