package de.dkutzer.tcgwatcher.cards.boundary


import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import de.dkutzer.tcgwatcher.ui.ClickableIconButton
import de.dkutzer.tcgwatcher.ui.ItemOfInterestCard
import de.dkutzer.tcgwatcher.ui.referrer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
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
fun SearchScreen(
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

    val searchResultPagingItems = searchViewModel.pokemonPagingDataFlow.collectAsLazyPagingItems()

    if (searchResultPagingItems.loadState.refresh is LoadState.Error) {
        LaunchedEffect(key1 = snackbarHostState) {
            snackbarHostState.showSnackbar(
                (searchResultPagingItems.loadState.refresh as LoadState.Error).error.message ?: ""
            )
        }
    }
    var showDialog by remember { mutableStateOf(false) }

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var backPressHandled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Use BackHandler to intercept the back button press
    BackHandler(enabled = !backPressHandled)  {
        showDialog = searchViewModel.onBack()
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text( stringResource( R.string.exit_app)) },
            text = { Text(stringResource(R.string.sure_exit)) },
            confirmButton = {
                Button(onClick = {
                    // Perform the action to exit the app
                    // For example, finish the activity
                    backPressHandled = true
                    coroutineScope.launch {
                        awaitFrame()
                        onBackPressedDispatcher?.onBackPressed()
                        backPressHandled = false
                    }
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }


    SearchView(
        searchResultPagingItems = searchResultPagingItems,
        historyList = searchViewModel.historyList,
        quickSearchList = searchViewModel.quickSearchList,
        isSearching = searchViewModel.showHistoryContent,
        onSearchQueryChange = { searchViewModel.onSearchQueryChange(it) },
        onSearchSubmit = { searchViewModel.onSearchSubmit(it) },
        onActiveChanged = { query, active -> searchViewModel.onActiveChanged(query, active) },
        onRefreshSearch = { searchViewModel.onRefreshSearch() },
        onRefreshSingleItem = { searchViewModel.onRefreshSingleItem(it) },
        onQuicksearchItemClick = { searchViewModel.onQuickSearch(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchView(
    searchResultPagingItems: LazyPagingItems<ProductModel>,
    historyList: StateFlow<List<HistorySearchItem>>,
    quickSearchList: StateFlow<List<QuickSearchItem>>,
    isSearching: StateFlow<Boolean>,

    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onActiveChanged: (String, Boolean) -> Unit,

    onRefreshSearch: () -> Unit,
    onRefreshSingleItem: (item: ProductModel) -> Unit,
    onQuicksearchItemClick: (item: ProductModel) -> Unit,
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
                Row(modifier = Modifier.padding(end = 8.dp)) {
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
//                        IconButton(
//                            onClick =
//                            {
//                                logger.debug { "SearchBar::trailingIcon:onClick: " }
//                                query = ""
//                                onSearchQueryChange("")
//                                onActiveChanged("", true)
//
//                            }) {
//                            Icon(
//                                imageVector = Icons.Default.Build,
//                                tint = MaterialTheme.colorScheme.onSurface,
//                                contentDescription = stringResource(id = R.string.clearSearch)
//                            )
//                        }
                    }

                }
            },
            onSearch = {
                logger.debug { "SearchBar::onSearch: $it" }
                onSearchSubmit(query)
            },
            active = active,
            onActiveChange = {
                logger.debug { "SearchBar:Active changed: $it" }
                onActiveChanged(query, it)
            },
            tonalElevation = 4.dp,
            content = {
                logger.debug { "SearchBar:Content:active: $active" }
                logger.debug { "SearchBar:Content:pokemonPagingItems: ${searchResultPagingItems.loadState.refresh}" }
                logger.debug { "SearchBar:Content:itemCount: ${searchResultPagingItems.itemCount}" }
                if (active) {
                    LazyColumn(
                        modifier = Modifier.weight(0.95f)
                    ) {
                        items(
                            count = historyItems.size + quickSearchItems.size
                        ) { index ->

                            if (index < historyItems.size) {
                                val item = historyItems[index]
                                Row(modifier = Modifier
                                    .padding(all = 4.dp)
                                    .clickable {
                                        logger.debug { "SearchBar:content:historyItemClick: $item" }
                                        query = item.displayName
                                        onSearchSubmit(item.displayName)
                                    }
                                )
                                {
                                    Icon(
                                        modifier = Modifier.padding(end = 8.dp),
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null
                                    )
                                    Text(text = item.displayName)
                                }
                            } else {
                                val item = quickSearchItems[index - historyItems.size]
                                Row(modifier = Modifier
                                    .padding(all = 4.dp)
                                    .clickable {
                                        logger.debug { "SearchBar:content:quicksearchItemClick: $item" }
                                        query = item.displayName
                                        onQuicksearchItemClick(item.toProductModel())
                                    }
                                )
                                {
                                    Icon(
                                        modifier = Modifier.padding(end = 8.dp),
                                        imageVector = Icons.Default.Star, contentDescription = null
                                    )
                                    Column(
                                        modifier = Modifier.padding(end = 1.dp)
                                    ) {
                                        Text(
                                            text = "${item.displayName} (${item.code})",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Row(modifier = Modifier.padding(end = 1.dp)) {
                                            val txt = "${item.nameDe} | ${item.nameEn} | ${item.nameFr}"
                                            Text(
                                                text = txt,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }

                                }
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

            if (searchResultPagingItems.loadState.refresh is LoadState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(128.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )


            } else {
                when (searchResultPagingItems.itemCount) {
                    0 -> NoSearchResults()
                    1 -> ItemCardDetailLayout(
                        productModel = searchResultPagingItems[0]!!,
                        onRefreshItemDetailsContent = { onRefreshSingleItem(searchResultPagingItems[0]!!) },
                        modifier = Modifier.fillMaxSize()
                    )

                    else -> {
                        ListDetailLayout(
                            productPagingItems = searchResultPagingItems,
                            onRefreshList = { onRefreshSearch() },
                            onRefreshDetails = {  item -> onRefreshSingleItem(item) },
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
    productPagingItems: LazyPagingItems<ProductModel>,
    onRefreshList: () -> Unit,
    onRefreshDetails: (ProductModel) -> Unit
) {

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    NavigableListDetailPaneScaffold(
        modifier = modifier,
        navigator = navigator,
        listPane = {

            PullToRefreshLazyColumn(
                modifier = modifier,
                onRefreshContent = { onRefreshList() },
                content = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(
                            count = productPagingItems.itemCount,
                            key = productPagingItems.itemKey { it.id }
                        )
                        { index ->
                            val productModel = productPagingItems[index]
                            ItemOfInterestCard(
                                productModel = productModel!!,
                                showLastUpdated = false,
                                iconRowContent = {  },
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
                }
            )

        },
        detailPane = {
            val content = navigator.currentDestination?.content //productModel
            if (content != null) {
                AnimatedPane {
                    ItemCardDetailLayout(
                        productModel = content as ProductModel,
                        onRefreshItemDetailsContent = { onRefreshDetails(content) }
                    )
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

    private val _lastQuery: MutableStateFlow<String> = MutableStateFlow("")
    private val lastQuery: StateFlow<String> = _lastQuery.asStateFlow()

    private val _query: MutableStateFlow<String> = MutableStateFlow("")
    private val query: StateFlow<String> = _query.asStateFlow()

    private val _refreshItem: MutableStateFlow<RefreshWrapper> = MutableStateFlow(RefreshWrapper(item = null, query = "", state = RefreshState.IDLE))
    private val refreshItem: StateFlow<RefreshWrapper> = _refreshItem.asStateFlow()

    private val _quicksearchItem: MutableStateFlow<ProductModel?> = MutableStateFlow(null)
    private val quicksearchItem: StateFlow<ProductModel?> = _quicksearchItem.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemonPagingDataFlow: Flow<PagingData<ProductModel>> =
        combine(query, refreshItem, quicksearchItem) { latestSearchQuery, latestRefreshItem, quicksearchItem ->
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
            _refreshItem.value = RefreshWrapper(item = null, query = _query.value, state = RefreshState.IDLE)
            return false
        }
        return  true

    }


    fun onRefreshSearch() {
        logger.debug { "SearchViewModel::onRefreshSearch" }
        _quicksearchItem.value = null
        _refreshItem.value = RefreshWrapper(item = null, query = _query.value, state = RefreshState.REFRESH_SEARCH)
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

@Composable
private fun NoSearchResults() {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        items(
            count = 1
        ) {

            Text(stringResource(id = R.string.emptySearch))
        }
    }
}


private const val USER_AGENT = "User-Agent"

private const val REFERER = "Referer"

@Composable
private fun ItemCardDetailLayout(
    productModel: ProductModel,
    modifier: Modifier = Modifier,
    onRefreshItemDetailsContent: (item: ProductModel) -> Unit,
) {
    val innerPadding = 1.dp

    PullToRefreshLazyColumn(
        modifier = modifier,
        onRefreshContent = { onRefreshItemDetailsContent(productModel) },
        content = {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
            ) {
                items(
                    count = 1
                ) {

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(productModel.imageUrl)
                            .setHeader(USER_AGENT, userAgent)
                            .setHeader(
                                REFERER,
                                referrer
                            )
                            .build(),

                        contentDescription = productModel.id,
                        modifier = Modifier
                            .padding(innerPadding)
                            .height(410.dp) //this sucks, but i dont know how to max the height and width of an image but keep the aspect ratio
                            .fillMaxWidth(),

                        contentScale = ContentScale.FillHeight,
                        imageLoader = LocalContext.current.imageLoader.newBuilder()
                            .logger(DebugLogger())
                            .build()
                    )

                    Card(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        elevation = CardDefaults.cardElevation()
                    ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(32.dp)
                                            .padding(4.dp),
                                        painter = painterResource(R.drawable.de_language_icon),
                                        contentDescription = stringResource(id = R.string.nameLabel)
                                    )

                                    Text(
                                        text = productModel.localName,
                                        style = MaterialTheme.typography.headlineMedium
                                    )

                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(32.dp)
                                            .padding(4.dp),
                                        painter = painterResource(R.drawable.globe_line_icon),
                                        contentDescription = stringResource(id = R.string.nameLabel)
                                    )
                                    Text(
                                        text = productModel.orgName,
                                        style = MaterialTheme.typography.headlineSmall
                                    )

                                }



                                Row (verticalAlignment = Alignment.CenterVertically)
                                {
                                    Icon(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(32.dp)
                                            .padding(4.dp),
                                        painter = painterResource(R.drawable.price_tag_euro_icon),
                                        contentDescription = stringResource(id = R.string.priceLabel)
                                    )
                                    Text(
                                        text = productModel.price,
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                }
                                Row (verticalAlignment = Alignment.CenterVertically){
                                    Icon(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .height(32.dp)
                                            .padding(4.dp),
                                        painter = painterResource(R.drawable.stock_market_icon),
                                        contentDescription = stringResource(id = R.string.priceLabel)
                                    )
                                    Text(
                                        text = productModel.priceTrend,
                                        style = MaterialTheme.typography.headlineSmall
                                    )

                                }




                    }
                }
            }
        }
    )


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshLazyColumn(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onRefreshContent: () -> Unit
) {
    val state = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            logger.debug{"PullToRefreshLazyColumn::onRefresh"}
            onRefreshContent()
            logger.debug{"PullToRefreshLazyColumn::delay"}
            delay(100)
            logger.debug{"PullToRefreshLazyColumn::done"}
            isRefreshing = false
        }
    }
    PullToRefreshBox(
        modifier = modifier.padding(8.dp),
        state = state,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    ) {
        content()
    }

}