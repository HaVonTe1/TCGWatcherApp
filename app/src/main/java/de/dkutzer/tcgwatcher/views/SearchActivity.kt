package de.dkutzer.tcgwatcher.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.room.util.query
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.products.adapter.PokemonPager
import de.dkutzer.tcgwatcher.products.adapter.api.CardmarketApiClientFactory
import de.dkutzer.tcgwatcher.products.adapter.port.GetPokemonList
import de.dkutzer.tcgwatcher.products.config.CardmarketConfig
import de.dkutzer.tcgwatcher.products.domain.*
import de.dkutzer.tcgwatcher.products.domain.port.*
import de.dkutzer.tcgwatcher.products.services.CardmarketPokemonRepositoryImpl
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils

private val logger = KotlinLogging.logger {}

@Composable
fun SearchActivity(
    snackbarHostState: SnackbarHostState
) {

    val context = LocalContext.current

    val settingsDatabase: SettingsDatabase by lazy {
        SettingsDatabase.getDatabase(context)
    }
    val searchCacheDatabase: SearchCacheDatabase by lazy {
        SearchCacheDatabase.getDatabase(context)
    }


    val searchViewModel = viewModel<SearchViewModel>(
        factory = SearchViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(SettingsDbIdKey, settingsDatabase)
            set(SearchCacheRepoIdKey, searchCacheDatabase)
        }
    )

    val pokemonPagingItems = searchViewModel.pokemonPagingDataFlow.collectAsLazyPagingItems()

    if (pokemonPagingItems.loadState.refresh is LoadState.Error) {
        LaunchedEffect(key1 = snackbarHostState) {
            snackbarHostState.showSnackbar(
                (pokemonPagingItems.loadState.refresh as LoadState.Error).error.message ?: ""
            )
        }
    }


    SearchView(
        pokemonPagingItems = pokemonPagingItems,
        historyList = searchViewModel.historyList,
        isSearching = searchViewModel.showHistoryContent,
        onSearchQueryChange = { searchViewModel.onSearchQueryChange(it) },
        onSearchSubmit = { searchViewModel.onSearchSubmit(it) },
        onActiveChanged = { query, active ->  searchViewModel.onActiveChanged(query,active) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchView(
    pokemonPagingItems: LazyPagingItems<SearchProductModel>,
    historyList: StateFlow<List<String>>,
    isSearching: StateFlow<Boolean>,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onActiveChanged: (String, Boolean) -> Unit
) {

    val listState = historyList.collectAsState()

    var query: String by remember { mutableStateOf("") }
    val active  by isSearching.collectAsState(initial = false)

    val historyItems by remember(listState.value) { mutableStateOf(listState.value) }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        SearchBar(
            query = query,
            onQueryChange = { text ->
                logger.debug { "SearchBar::OnQueryChange: $text" }
                query = text

                onSearchQueryChange(text)

            },
            placeholder = {
                Text(text = stringResource(id = R.string.searchPlaceHolder))
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick =
                        {
                            logger.debug { "SearchBar::trailingIcon:onClick: " }
                            query = ""
                            onSearchQueryChange("")
                            onActiveChanged("", true)

                        }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = stringResource(id = R.string.clearSearch)
                        )
                    }
                }
            },
            onSearch = {
                logger.debug { "SearchBar::onSearch: $it" }
                onSearchSubmit(query.uppercase())
            },
            active = active,
            onActiveChange = {
                logger.debug { "SearchBar:Active changed: $it" }
                onActiveChanged(query.uppercase(), it)
            },
            tonalElevation = 4.dp,
            content = {
                logger.debug { "SearchBar:Content:active: $active" }
                logger.debug { "SearchBar:Content:pokemonPagingItems: ${pokemonPagingItems.loadState.refresh}" }
                logger.debug { "SearchBar:Content:itemCount: ${pokemonPagingItems.itemCount}" }
                if (active) {
                    LazyColumn(
                        modifier = Modifier.weight(0.95f)
                    ) {
                        items(
                            count = historyItems.size
                        ) { index ->
                            val item = historyItems[index]
                            Row(modifier =
                            Modifier
                                .padding(all = 16.dp)
                                .clickable {
                                    logger.debug { "SearchBar:content:historyItemClick: $item" }
                                    query = item
                                    onSearchSubmit(query.uppercase())
                                }
                            )

                            {
                                Icon(
                                    modifier = Modifier.padding(end = 12.dp),
                                    imageVector = Icons.Default.Search, contentDescription = null
                                )
                                Text(text = item)
                            }

                        }
                    }

                }

            }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            Arrangement.Center,
            CenterHorizontally
        ) {

            if (pokemonPagingItems.loadState.refresh is LoadState.Loading) {


                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                return

            } else {
                if (pokemonPagingItems.itemCount == 0) {
                    NoSearchResults()
                } else {

                    LazyColumn(
                        modifier = Modifier.weight(0.95f)
                    ) {
                        items(
                            count = pokemonPagingItems.itemCount,
                            key = pokemonPagingItems.itemKey { it.id }) { index ->
                            val productModel = pokemonPagingItems[index]
                            ItemOfInterestCard(
                                productModel = productModel as BaseProductModel,
                                showLastUpdated = false,
                                iconRowContent = { SearchViewCardIconRow() },
                            )
                        }
                    }
                }
            }
        }


    }
}

@Composable
fun SearchViewCardIconRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(1.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        ClickableIconButton(
            icon = Icons.TwoTone.Add,
            desc = stringResource(id = R.string.addDesc),
            onClick = {})
    }
}


class SearchViewModel(
    private val settingsDatabase: SettingsDatabase,
    private val searchCacheDatabase: SearchCacheDatabase,
) : ViewModel() {


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


    private val _query: MutableStateFlow<String> = MutableStateFlow("")
    private val query: StateFlow<String> = _query.asStateFlow()

    private val _lastQuery: MutableStateFlow<String> = MutableStateFlow("")
    private val lastQuery: StateFlow<String> = _lastQuery.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemonPagingDataFlow: Flow<PagingData<SearchProductModel>> =
        query.flatMapLatest { latestSearchQueryFromFlow ->

            logger.debug { "SesrchViewModel:: Flow of query changed: $latestSearchQueryFromFlow" }
            val config = CardmarketConfig(settings.value)

            val productApiClient = CardmarketApiClientFactory(config).create()
            val pokemonPager =
                PokemonPager.providePokemonPager(
                    latestSearchQueryFromFlow,
                    searchCacheDatabase,
                    productApiClient
                )
            val pokemonRepositoryImpl = CardmarketPokemonRepositoryImpl(pokemonPager)
            logger.debug { "getPokemonList now" }
            val getPokemonList = GetPokemonList(pokemonRepositoryImpl)
            getPokemonList().cachedIn(viewModelScope)
        }


    private var unfilteredHistoryItems: ArrayList<String> = arrayListOf()
    private val _historyList = MutableStateFlow<List<String>>(mutableListOf()) //Changed
    val historyList: StateFlow<List<String>> =

        query.combine(_historyList) { text, historyItems ->
            logger.debug { "history combine: $text and $historyItems" }
            historyItems
        }.stateIn(//basically convert the Flow returned from combine operator to StateFlow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),//it will allow the StateFlow survive 5 seconds before it been canceled
            initialValue = listOf("")
        )


    init {
        logger.debug { "SearchViewModel::init" }
        viewModelScope.launch(Dispatchers.IO) {
            val settingsEntity = SettingsRepositoryImpl(settingsDatabase.settingsDao).load()
            logger.debug { "SettingsEntity: $settingsEntity" }
            _settings.value = settingsEntity

            val searchHistory =
                SearchCacheRepositoryImpl(searchCacheDatabase.searchCacheDaoDa).getSearchHistory()

            logger.debug { "searchHistory: $searchHistory" }
            unfilteredHistoryItems.addAll(searchHistory)
            _historyList.value = searchHistory
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
                logger.info { "Creating SearchViewModel" }
                val settingsRepo = extras[SettingsDbIdKey]
                val searchCacheRepository = extras[SearchCacheRepoIdKey]

                return SearchViewModel(
                    settingsRepo!!,
                    searchCacheRepository!!,
                ) as T
            }
        }
    }


    fun onSearchQueryChange(newQuery: String) {
        logger.debug { "SearchViewModel::onSearchQueryChange: $newQuery" }

        _historyList.value =
            if (newQuery.isBlank()) { //return the entery list of countries if not is typed
                unfilteredHistoryItems
            } else {

                val filteredHistoryItems =
                    unfilteredHistoryItems.filter { historyItem ->// filter and return a list of countries based on the text the user typed
                        historyItem.uppercase().contains(newQuery.trim().uppercase())
                    }
                logger.debug { "filter result: $filteredHistoryItems" }
                filteredHistoryItems
            }

    }


    fun onSearchSubmit(searchString: String) {
        logger.debug { "SearchViewModel::onSearchSubmit: $searchString" }
        if (searchString.isEmpty()) {
            logger.debug { "Empty search" }
            return
        }
        if (!unfilteredHistoryItems.contains(searchString))
            unfilteredHistoryItems.add(searchString)
        _historyList.value = unfilteredHistoryItems
        _query.value = searchString
        _lastQuery.value = searchString
        onActiveChanged(searchString, false)
    }

    fun onActiveChanged(query:String, active: Boolean) {
        logger.debug { "SearchModel::onActiveChanged: current show history =  ${_showHistoryContent.value}" }
        logger.debug { "SearchModel::onActiveChanged: propagated active =  ${active}" }
        logger.debug { "SearchModel::onActiveChanged: query =  $query" }
        logger.debug { "SearchModel::onActiveChanged: lastQuery  = ${_lastQuery.value}" }
        logger.debug { "SearchModel::onActiveChanged: filteredHistoryList  = ${_historyList.value}" }

        val determineShowHistory = determineShowHistory(query, active)
        logger.debug { "SearchModel::onActiveChanged: new show history =  ${determineShowHistory}" }
        _showHistoryContent.value = determineShowHistory

    }

    private fun determineShowHistory(query: String, active: Boolean): Boolean {
        if(query.isBlank() && active) { //reset
            _lastQuery.value = ""
        }
        if(query.isBlank() && _lastQuery.value.isBlank()) {
            return true
        }
        if(query.isBlank())
            return false
        if(_historyList.value.isEmpty())
            return false

        if(StringUtils.equals(query, lastQuery.value))
            return false

        return true
    }

}

@Composable
private fun NoSearchResults() {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        Text(stringResource(id = R.string.emptySearch))
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun TestSearchPreview() {
//
//  //  searchViewModel.searchResults = (Datasource().loadMockSearchData())
//    SearchActivity()
//
//}