package de.dkutzer.tcgwatcher.collectables.search.presentation


import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.collectables.history.data.SearchCacheDatabase
import de.dkutzer.tcgwatcher.collectables.quicksearch.data.QuickSearchDatabase
import de.dkutzer.tcgwatcher.collectables.search.presentation.components.SearchView
import de.dkutzer.tcgwatcher.settings.data.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.presentation.SettingModelCreationKeys
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch


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
            set(SettingModelCreationKeys.SettingsDbIdKey, settingsDatabase)
            set(SearchModelCreationKeys.SearchCacheRepoIdKey, searchCacheDatabase)
            set(SearchModelCreationKeys.QuickSearchRepoIdKey, quicksearchDatabase)
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
        modifier = Modifier.fillMaxSize().padding(0.dp),
        searchResultPagingItems = searchResultPagingItems,
        historyList = searchViewModel.historyList,
        quickSearchList = searchViewModel.quickSearchList,
        isSearching = searchViewModel.showHistoryContent,
        settings = searchViewModel.settings,
        onSearchQueryChange = { searchViewModel.onSearchQueryChange(it) },
        onSearchSubmit = { searchViewModel.onSearchSubmit(it) },
        onActiveChanged = { query, active -> searchViewModel.onActiveChanged(query, active) },
        onRefreshSearch = { searchViewModel.onRefreshSearch() },
        onRefreshSingleItem = { searchViewModel.onRefreshSingleItem(it) },
        onQuicksearchItemClick = { searchViewModel.onQuickSearch(it) }
    )
}


@PreviewLightDark
@Composable
fun SearchScreenPreview(modifier: Modifier = Modifier) {

    TCGWatcherTheme {
        SearchScreen(SnackbarHostState())

    }
}