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
import com.example.compose.TCGWatcherTheme
import de.dkutzer.tcgwatcher.collectables.search.domain.HistorySearchItem
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.QuickSearchItem
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.flowOf
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Composable
fun SearchPreviewContent(
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
            searchResultPagingItems = flowOf(PagingData.from(listOf(productModel))).collectAsLazyPagingItems(),
            historyItems = listOf(historySearchItem
            ),
            quickSearchItems = listOf(),
            onHistorieItemClicked = {},
            onQuickSearchItemClicked = {}
        )
    }
}