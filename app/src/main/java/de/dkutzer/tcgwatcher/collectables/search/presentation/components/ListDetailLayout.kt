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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.Instant

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ListDetailLayout(
    modifier: Modifier = Modifier,
    productPagingItems: LazyPagingItems<ProductModel>,
    onRefreshList: () -> Unit,
    onRefreshDetails: (ProductModel) -> Unit
) {

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val coroutineScope = rememberCoroutineScope() // Get a CoroutineScope tied to this composable

    NavigableListDetailPaneScaffold(
        modifier = modifier,
        navigator = navigator,
        listPane = {

//            PullToRefreshLazyColumn(
//                modifier = modifier,
//                onRefreshContent = { onRefreshList() },
//                content = {
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
                                        coroutineScope.launch {
                                            navigator.navigateTo(
                                                ListDetailPaneScaffoldRole.Detail,
                                                productModel
                                            )
                                        }
                                    }
                            )
                        }
                    }
                //}
           // )

        },
        detailPane = {
            navigator.currentDestination?.contentKey?.let {
                AnimatedPane {
                    ItemCardDetailLayout(
                        productModel = it as ProductModel,
                        onRefreshItemDetailsContent = { onRefreshDetails(it) },
                        onImageClick = {
                            coroutineScope.launch {
                                navigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Extra,
                                    it
                                )
                            }
                        },
                        onBackClick = {
                            coroutineScope.launch {
                                navigator.navigateBack()
                            }
                        }
                    )
                }
            }

        },
        extraPane = {
           navigator.currentDestination?.contentKey?.let {
                AnimatedPane {
                    ZoomableCardImage (
                        productModel = it as ProductModel,
                        onImageClick = {}
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
        ListDetailLayout(
            modifier = modifier,
            productPagingItems = flowOf(productModelPagingData).collectAsLazyPagingItems(),
            onRefreshList = {},
            onRefreshDetails = {},
        )

    }
}