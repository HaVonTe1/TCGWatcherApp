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
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch


private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SearchResultItemListView(
    modifier: Modifier = Modifier,
    productPagingItems: LazyPagingItems<ProductModel>,
    onRefreshList: () -> Unit,
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
            navigator.currentDestination?.contentKey?.let { index ->
                logger.debug { "DetailPane: $index" }

                AnimatedPane {
                    ProductDetailsView(
                        initialProduct = productPagingItems[index as Int]!!,
                        currentIndex = index,
                        nextIndex = if ((index + 1) < productPagingItems.itemCount) index + 1 else null,
                        previousIndex = if ((index - 1) >= 0) index - 1 else null,
                        onImageClick = { clickedIndex ->
                            logger.debug { "DetailPane:onImageClick: $clickedIndex" }
                            coroutineScope.launch {
                                navigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Extra,
                                    clickedIndex
                                )
                            }
                        },
                        onBackClick = {
                            logger.debug { "DetailPane:onBackClick:" }
                            coroutineScope.launch {
                                navigator.navigateBack()
                            }
                        },
                        onChangedIndex = { clickedIndex ->
                            logger.debug { "DetailPane:onPreviousClick: $clickedIndex" }
                            coroutineScope.launch {
                                navigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    clickedIndex
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
                    ZoomableCardImage(
                        productModel = productPagingItems[index]!!
                    )
                }
            }
        }
    )
}
