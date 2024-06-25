package de.dkutzer.tcgwatcher.cards.boundary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.cards.control.CardmarketPokemonRepositoryAdapter
import de.dkutzer.tcgwatcher.cards.control.GetPokemonList
import de.dkutzer.tcgwatcher.cards.control.cache.PokemonPager
import de.dkutzer.tcgwatcher.cards.control.cache.SearchCacheDatabase
import de.dkutzer.tcgwatcher.cards.control.cache.SearchCacheRepositoryImpl
import de.dkutzer.tcgwatcher.cards.control.quicksearch.QuickSearchDatabase
import de.dkutzer.tcgwatcher.cards.control.quicksearch.QuickSearchRepositoryImpl
import de.dkutzer.tcgwatcher.cards.entity.BaseProductModel
import de.dkutzer.tcgwatcher.cards.entity.CardmarketConfig
import de.dkutzer.tcgwatcher.cards.entity.SearchProductModel
import de.dkutzer.tcgwatcher.settings.control.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.control.SettingsRepositoryImpl
import de.dkutzer.tcgwatcher.settings.entity.Engines
import de.dkutzer.tcgwatcher.settings.entity.Languages
import de.dkutzer.tcgwatcher.settings.entity.QuickSearchRepoIdKey
import de.dkutzer.tcgwatcher.settings.entity.SearchCacheRepoIdKey
import de.dkutzer.tcgwatcher.settings.entity.SettingsDbIdKey
import de.dkutzer.tcgwatcher.settings.entity.SettingsEntity
import de.dkutzer.tcgwatcher.ui.ClickableIconButton
import de.dkutzer.tcgwatcher.ui.ItemOfInterestCard
import de.dkutzer.tcgwatcher.ui.referrer
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
import okhttp3.internal.userAgent
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

    val quicksearchDatabase: QuickSearchDatabase by lazy {
        QuickSearchDatabase.getDatabase(context)
    }


    val searchViewModel = viewModel<SearchViewModel>(
        factory = SearchViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(SettingsDbIdKey, settingsDatabase)
            set(SearchCacheRepoIdKey, searchCacheDatabase)
            set(QuickSearchRepoIdKey, quicksearchDatabase)
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
        quickSearchList = searchViewModel.quickSearchList,
        isSearching = searchViewModel.showHistoryContent,
        onSearchQueryChange = { searchViewModel.onSearchQueryChange(it) },
        onSearchSubmit = { searchViewModel.onSearchSubmit(it) },
        onActiveChanged = { query, active -> searchViewModel.onActiveChanged(query, active) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchView(
    pokemonPagingItems: LazyPagingItems<SearchProductModel>,
    historyList: StateFlow<List<String>>,
    quickSearchList: StateFlow<List<String>>,
    isSearching: StateFlow<Boolean>,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onActiveChanged: (String, Boolean) -> Unit
) {

    val historyListState = historyList.collectAsState()
    val quickSearchListState = quickSearchList.collectAsState()

    var query: String by remember { mutableStateOf("") }
    val active by isSearching.collectAsState(initial = false)

    val historyItems by remember(historyListState.value) { mutableStateOf(historyListState.value) }

    val quickSearchItems by remember(quickSearchListState.value) { mutableStateOf(quickSearchList.value) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        androidx.compose.material.MaterialTheme.colors.primary,
                        androidx.compose.material.MaterialTheme.colors.secondary
                    )
                )
            )
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
                            count = historyItems.size + quickSearchItems.size
                        ) { index ->

                            val item = if (index < historyItems.size) {
                                historyItems[index]
                            } else {
                                quickSearchItems[index - historyItems.size]
                            }
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

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp))
                    )
                    { innerPadding ->
                        ListDetailLayout(
                            modifier = Modifier.padding(innerPadding),
                            pokemonPagingItems
                        )
                    }

                }
            }
        }


    }
}


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ListDetailLayout(
    modifier: Modifier = Modifier,
    pokemonPagingItems: LazyPagingItems<SearchProductModel>
) {

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    NavigableListDetailPaneScaffold(
        modifier = modifier,
        navigator = navigator,
        listPane = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(
                    count = pokemonPagingItems.itemCount,
                    key = pokemonPagingItems.itemKey { it.id }
                )
                { index ->
                    val productModel = pokemonPagingItems[index]
                    ItemOfInterestCard(
                        productModel = productModel as BaseProductModel,
                        showLastUpdated = false,
                        iconRowContent = { SearchViewCardIconRow() },
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .clickable {
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    content = productModel
                                )
                            }
                    )
                }
            }
        },
        detailPane = {
            val content = navigator.currentDestination?.content //productModel
            if(content!=null) {
                AnimatedPane {
                    ItemCardDetailLayout(productModel = content as BaseProductModel)
                }

            }
        }

    )
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
            val pokemonRepositoryImpl = CardmarketPokemonRepositoryAdapter(pokemonPager)
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

    private val _quickSearchList = MutableStateFlow<List<String>>(mutableListOf())
    val quickSearchList: StateFlow<List<String>> = _quickSearchList.asStateFlow()


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

        viewModelScope.launch(Dispatchers.IO) {
            _quickSearchList.value =
                if (newQuery.isBlank()) {
                    emptyList()
                } else {

                    logger.debug { "query quick search" }
                    val pokemonCardQuickEntities = quicksearchRepository.find(newQuery)
                    logger.debug { "pokemonCardQuickEntities: $pokemonCardQuickEntities" }
                    val result = pokemonCardQuickEntities.map { "${it.nameDe} (${it.code})" }
                    logger.debug { "$result" }
                    result
                }
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
        _quickSearchList.value = emptyList()
        onActiveChanged(searchString, false)
    }

    fun onActiveChanged(query: String, active: Boolean) {
        logger.debug { "SearchModel::onActiveChanged: current show history =  ${_showHistoryContent.value}" }
        logger.debug { "SearchModel::onActiveChanged: propagated active =  $active" }
        logger.debug { "SearchModel::onActiveChanged: query =  $query" }
        logger.debug { "SearchModel::onActiveChanged: lastQuery  = ${_lastQuery.value}" }
        logger.debug { "SearchModel::onActiveChanged: filteredHistoryList  = ${_historyList.value}" }

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

@Preview(showBackground = false, showSystemUi = true)
@Composable
fun ItemCardDetailLayoutPreview() {

  //  searchViewModel.searchResults = (Datasource().loadMockSearchData())
    ItemCardDetailLayout(
        productModel =  BaseProductModel(
            id = "bla",
            imageUrl = "https://product-images.s3.cardmarket.com/51/TEF/760774/760774.jpg",
            intPrice = "10,00 €",
            localName = "bbbbb",
            detailsUrl = "https://product-images.s3.cardmarket.com/51/TEF/760774/760774.jpg"
        ),
    )

}

@Composable
private fun ItemCardDetailLayout(
    productModel: BaseProductModel,
    modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
       // verticalArrangement = Arrangement.Center

    ) {
        Row (
            modifier = modifier
                .padding(1.dp)
                .fillMaxWidth()
                .fillMaxHeight(.8f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            AsyncImage(

                model = ImageRequest.Builder(LocalContext.current)
                    .data(productModel.imageUrl)
                    .setHeader("User-Agent", userAgent)
                    .setHeader("Referer", referrer) //TODO: cloudflare protection is kicking in without the referer
                    .build(),

                contentDescription = productModel.id,
                modifier = modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
                    //.align(Alignment.CenterHorizontally)
                    .padding(1.dp),
                contentScale = ContentScale.FillHeight,
                imageLoader = LocalContext.current.imageLoader.newBuilder().logger(DebugLogger()).build()
            )
        }
        Row (
            modifier = modifier
                .padding(1.dp)
                .fillMaxWidth()
                .fillMaxHeight(.2f)
        ) {
            Text(text = productModel.localName)
        }
    }

}