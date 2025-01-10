package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.collectables.search.domain.HistorySearchItem
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.QuickSearchItem
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.StateFlow


private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
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
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { text ->
                        logger.debug { "SearchBar::OnQueryChange: $text" }
                        query = text
                        onSearchQueryChange(text)
                    },
                    onSearch = {
                        logger.debug { "SearchBar::onSearch: $it" }
                        keyboardController?.hide()
                        onSearchSubmit(query)
                    },
                    expanded = active,
                    onExpandedChange = {
                        logger.debug { "SearchBar:Active changed: $it" }
                        onActiveChanged(query, it)
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
                )
            },
            expanded = active,
            onExpandedChange = {
                logger.debug { "SearchBar:Active changed: $it" }
                onActiveChanged(query, it)
            },

            content = {
                SearchPreviewContent(
                    active,
                    searchResultPagingItems,
                    historyItems,
                    quickSearchItems,
                    onSearchHistoryItemClicked = {
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
            Alignment.CenterHorizontally
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