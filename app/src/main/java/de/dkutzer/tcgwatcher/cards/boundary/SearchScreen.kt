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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.cards.control.cache.SearchCacheDatabase
import de.dkutzer.tcgwatcher.cards.control.quicksearch.QuickSearchDatabase
import de.dkutzer.tcgwatcher.cards.entity.HistorySearchItem
import de.dkutzer.tcgwatcher.cards.entity.ProductModel
import de.dkutzer.tcgwatcher.cards.entity.QuickSearchItem
import de.dkutzer.tcgwatcher.settings.control.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.entity.QuickSearchRepoIdKey
import de.dkutzer.tcgwatcher.settings.entity.SearchCacheRepoIdKey
import de.dkutzer.tcgwatcher.settings.entity.SettingsDbIdKey
import de.dkutzer.tcgwatcher.ui.ItemOfInterestCard
import de.dkutzer.tcgwatcher.ui.referrer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent

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
    BackHandler(enabled = !backPressHandled) {
        showDialog = searchViewModel.onBack()
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.exit_app)) },
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

    val keyboardController = LocalSoftwareKeyboardController.current

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
                SearchTrailingIcon(query, onClick = {
                    logger.debug { "SearchBar::trailingIcon:onClick: " }
                    query = ""
                    onSearchQueryChange("")
                    onActiveChanged("", true)
                })
            },
            onSearch = {
                logger.debug { "SearchBar::onSearch: $it" }
                keyboardController?.hide()
                onSearchSubmit(query)
            },
            active = active,
            onActiveChange = {
                logger.debug { "SearchBar:Active changed: $it" }
                onActiveChanged(query, it)
            },
            tonalElevation = 4.dp,
            content = {
                SearchPreviewContent(
                    active,
                    searchResultPagingItems,
                    historyItems,
                    quickSearchItems,
                    onHistorieItemClicked = {
                        logger.debug { "SearchBar:content:historyItemClick: $it" }
                        query = it
                        onSearchSubmit(it)
                    },
                    onQuickSearchItemClicked = {
                        logger.debug { "SearchBar:content:quicksearchItemClick: $it" }
                        query = it.displayName
                        onQuicksearchItemClick(it.toProductModel())
                    }
                )
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
                            onRefreshDetails = { item -> onRefreshSingleItem(item) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPreviewContent(
    active: Boolean,
    searchResultPagingItems: LazyPagingItems<ProductModel>,
    historyItems: List<HistorySearchItem>,
    quickSearchItems: List<QuickSearchItem>,
    onHistorieItemClicked: (String) -> Unit,
    onQuickSearchItemClicked: (QuickSearchItem) -> Unit,
) {

    logger.debug { "SearchBar:Content:active: $active" }
    logger.debug { "SearchBar:Content:pokemonPagingItems: ${searchResultPagingItems.loadState.refresh}" }
    logger.debug { "SearchBar:Content:itemCount: ${searchResultPagingItems.itemCount}" }
    if (active) {
        LazyColumn {
            items(
                count = historyItems.size + quickSearchItems.size
            ) { index ->

                if (index < historyItems.size) {
                    val item = historyItems[index]
                    Row(modifier = Modifier
                        .padding(all = 4.dp)
                        .clickable { onHistorieItemClicked(item.displayName) }
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
                        .clickable { onQuickSearchItemClicked(item) }
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

@Composable
private fun SearchTrailingIcon(
    query: String,
    onClick: () -> Unit,
) {

    Row(modifier = Modifier.padding(end = 8.dp)) {
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onClick() }
            )
            {
                Icon(
                    imageVector = Icons.Default.Close,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(id = R.string.clearSearch)
                )
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
                                iconRowContent = { },
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
                            .fillMaxWidth(),

                        contentScale = ContentScale.FillWidth,
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
                        IconWithText(painterResource(R.drawable.de_language_icon),stringResource(id = R.string.nameLabel),productModel.localName, MaterialTheme.typography.headlineMedium)
                        IconWithText(painterResource(R.drawable.globe_line_icon),stringResource(id = R.string.nameLabel),productModel.orgName, MaterialTheme.typography.headlineSmall)
                        IconWithText(painterResource(R.drawable.price_tag_euro_icon),stringResource(id = R.string.priceLabel),productModel.price, MaterialTheme.typography.headlineLarge)
                        IconWithText(painterResource(R.drawable.stock_market_icon),stringResource(id = R.string.priceLabel),productModel.priceTrend, MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    )
}

@Composable
private fun IconWithText(
    icon: Painter,
    desc: String,
    text: String,
    testStyle: TextStyle
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .padding(4.dp),
            painter = icon,
            contentDescription = desc
        )

        Text(
            text = text,
            style = testStyle
        )

    }
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
            logger.debug { "PullToRefreshLazyColumn::onRefresh" }
            onRefreshContent()
            logger.debug { "PullToRefreshLazyColumn::delay" }
            delay(100)
            logger.debug { "PullToRefreshLazyColumn::done" }
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