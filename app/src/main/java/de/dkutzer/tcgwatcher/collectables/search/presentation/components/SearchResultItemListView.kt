package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.Instant


private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SearchResultItemListView(
    modifier: Modifier = Modifier,
    productPagingItems: LazyPagingItems<ProductModel>,
    onRefreshList: () -> Unit,
    onRefreshDetails: (ProductModel) -> Unit
) {

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val coroutineScope = rememberCoroutineScope()

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
                            logger.debug { "ProductListViewItemView: $index" }
                            val productModel = productPagingItems[index]
                            ProductListViewItemView(
                                productModel = productModel!!,
                                showLastUpdated = false,
                                iconRowContent = { },
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            navigator.navigateTo(
                                                ListDetailPaneScaffoldRole.Detail,
                                                index
                                            )
                                        }
                                    }
                            )
                        }
                    }
                }
            )

        },
        detailPane = {
            navigator.currentDestination?.contentKey?.let {
                logger.debug { "DetailPane: $it" }
                AnimatedPane {
                    val index = it as Int;
                    ProductDetailsView(
                        products = productPagingItems,
                        index = index,
                        refreshProductDetails = { onRefreshDetails(it) },
                        onImageClick = {
                            logger.debug { "DetailPane:onImageClick: $it" }
                            coroutineScope.launch {
                                navigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Extra,
                                    it
                                )
                            }
                        },
                        onBackClick = {
                            logger.debug { "DetailPane:onBackClick: $it" }
                            coroutineScope.launch {
                                navigator.navigateBack()
                            }
                        },
                        onIndexChange = { newIndex ->
                            coroutineScope.launch {
                                // Navigate to the new index within the detail pane
                                navigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    newIndex
                                )
                            }
                        }

                    )
                }
            }
        },
        extraPane = {
           navigator.currentDestination?.contentKey?.let {
               logger.debug { "ExtraPane: $it" }
                AnimatedPane {
                    val index = it as Int;
                    ZoomableCardImage (
                        productModel = productPagingItems[index]!!
                    )
                }
            }
        }
    )
}

@Composable
@PreviewLightDark
fun ListDetailLayoutPreview(modifier: Modifier = Modifier) {
    TCGWatcherTheme {

        val productModelPagingData = PagingData.from(
            listOf(
                ProductModel(
                    detailsUrl = "detailsUrl",
                    id = "1",
                    localName = "df",
                    code = "dfg",
                    orgName = "dfg",
                    imageUrl = "dfg",
                    price = "34r",
                    priceTrend = "df",
                    timestamp = Instant.now().epochSecond,
                )

            )
        )
        SearchResultItemListView(
            modifier = modifier,
            productPagingItems = flowOf(productModelPagingData).collectAsLazyPagingItems(),
            onRefreshList = {},
            onRefreshDetails = {},
        )

    }
}