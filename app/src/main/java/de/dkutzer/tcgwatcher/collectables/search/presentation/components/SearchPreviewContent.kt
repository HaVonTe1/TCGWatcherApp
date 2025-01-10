package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import de.dkutzer.tcgwatcher.collectables.search.domain.HistorySearchItem
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.QuickSearchItem
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.flowOf
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Composable
fun SearchPreviewContent(
    active: Boolean,
    products: LazyPagingItems<ProductModel>,
    searchHistory: List<HistorySearchItem>,
    quickSearches: List<QuickSearchItem>,
    onSearchHistoryItemClicked: (String) -> Unit,
    onQuickSearchItemClicked: (QuickSearchItem) -> Unit,
) {

    logger.debug { "SearchBar:Content:active: $active" }
    logger.debug { "SearchBar:Content:pokemonPagingItems: ${products.loadState.refresh}" }
    logger.debug { "SearchBar:Content:itemCount: ${products.itemCount}" }
    if (active) {
        LazyColumn {
            items(searchHistory.size) { index ->
                val item = searchHistory[index]
                SearchItem(
                    icon = Icons.Default.Search,
                    text = item.displayName,
                    onClick = { onSearchHistoryItemClicked(item.displayName) }
                )
            }
            items(quickSearches.size) { index ->
                val item = quickSearches[index]
                SearchItem(
                    icon = Icons.Default.Star,
                    text = item.displayName,
                    secondaryText = "${item.nameDe} | ${item.nameEn} | ${item.nameFr}",
                    onClick = { onQuickSearchItemClicked(item) }
                )
            }
        }
    }
}

@Composable
private fun SearchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    secondaryText: String? = null,
    onClick: () -> Unit
) {
    Row(modifier = Modifier
        .padding(all = 4.dp)
        .clickable { onClick() }
    ) {
        Icon(
            modifier = Modifier.padding(end = 8.dp),
            imageVector = icon,
            contentDescription = null
        )
        Column {
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
            if (secondaryText != null) {
                Text(text = secondaryText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
@PreviewLightDark
fun SearchPreviewContentPreview(modifier: Modifier = Modifier) {
    val productModel = ProductModel(
        "test",
        "test",
        "test",
        "test",
        "test",
        "test",
        "test",
        "test",
        Instant.now().epochSecond,
    )

    val historySearchItem = HistorySearchItem(
        "test"
    )
    TCGWatcherTheme {
        SearchPreviewContent(
            active = true,
            products = flowOf(PagingData.from(listOf(productModel))).collectAsLazyPagingItems(),
            searchHistory = listOf(historySearchItem
            ),
            quickSearches = listOf(),
            onSearchHistoryItemClicked = {},
            onQuickSearchItemClicked = {}
        )
    }
}