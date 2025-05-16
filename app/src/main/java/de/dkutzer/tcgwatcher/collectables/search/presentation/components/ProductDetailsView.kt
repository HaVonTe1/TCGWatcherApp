package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import android.content.Intent
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.automirrored.twotone.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.twotone.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.collectables.search.data.REFERER
import de.dkutzer.tcgwatcher.collectables.search.data.USER_AGENT
import de.dkutzer.tcgwatcher.collectables.search.data.referrer
import de.dkutzer.tcgwatcher.collectables.search.data.userAgent
import de.dkutzer.tcgwatcher.collectables.search.domain.OfferFilters
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import kotlin.math.roundToInt

private enum class Anchors { Left, Center, Right }

/**
 * Composable function that displays the detailed information of a product item in a card layout.
 *
 * @param product The [ProductModel] object containing the details of the product to be displayed.
 * @param modifier Optional [Modifier] for customizing the layout.
 * @param refreshProductDetails Callback function to refresh the product details.  It takes the current [ProductModel] as a parameter.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductDetailsView(
    products: LazyPagingItems<ProductModel>,
    index: Int,
    modifier: Modifier = Modifier,
    refreshProductDetails: (product: ProductModel) -> Unit,
    onImageClick: (index: Int) -> Unit = {},
    onBackClick: () -> Unit = {},
    onIndexChange: (Int) -> Unit = {}
) {

    val productModel = products[index]

    var currentProductModel by remember(productModel!!) { mutableStateOf(productModel) }
    var currentIndex by remember(index) { mutableIntStateOf(index) }
    // Function to handle index changes
    fun updateIndex(newIndex: Int) {
        currentIndex = newIndex
        onIndexChange(newIndex) // Notify parent of index change
    }
    val context = LocalContext.current
    val intent =
        remember { Intent(Intent.ACTION_VIEW, (referrer + productModel.detailsUrl).toUri()) }


    LazyColumn(
        modifier = Modifier
            .padding(1.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        items(
            count = 1
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(1.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .clickable { onBackClick() }
                        .align(Alignment.CenterVertically)
                        .padding(4.dp)
                        .size(24.dp),
                    imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                    contentDescription = "Back"
                )

                Text(
                    text = "${currentProductModel.name.value} (${currentProductModel.code})",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            context.startActivity(intent)
                        }
                        .align(Alignment.CenterVertically)
                        .weight(1f),  // Takes remaining space
                    textAlign = TextAlign.Center,  // Centers text within allocated space
                    maxLines = 1,  // Prevents line breaks
                    overflow = TextOverflow.Ellipsis  // Adds "..." when text overflows
                )
                Icon(
                    modifier = Modifier
                        .clickable { refreshProductDetails(currentProductModel) }
                        .align(Alignment.CenterVertically)
                        .padding(4.dp)
                        .size(24.dp),
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
            Row {
                if (currentIndex - 1 >= 0) {
                    Icon(
                        modifier = Modifier
                            .clickable {
                                updateIndex(currentIndex - 1)
                            }
                            .align(Alignment.CenterVertically)
                            .padding(4.dp)
                            .size(24.dp),
                        imageVector = Icons.AutoMirrored.TwoTone.KeyboardArrowLeft,
                        contentDescription = "Previous"
                    )
                }

                val screenWidthPx =
                    with(LocalDensity.current) {
                        LocalConfiguration.current.screenWidthDp.dp.roundToPx().toFloat()
                    }

                val dragState: AnchoredDraggableState<Anchors> = remember {
                    AnchoredDraggableState(
                        initialValue = Anchors.Center,
                        anchors = DraggableAnchors {
                            Anchors.Left at -screenWidthPx * 3 / 4
                            Anchors.Center at 0f
                            Anchors.Right at screenWidthPx * 3 / 4
                        },
                        positionalThreshold = { _ -> screenWidthPx / 3 },
                        velocityThreshold = { 100f },
                        snapAnimationSpec = tween(),
                        decayAnimationSpec = exponentialDecay()
                    )
                }

                // Handle drag state changes
                LaunchedEffect(dragState.currentValue) {
                    when (dragState.currentValue) {
                        Anchors.Right -> {
                            if (currentIndex - 1 >= 0) {
                                updateIndex(currentIndex - 1)
                            }
                            dragState.snapTo(Anchors.Center)
                        }

                        Anchors.Left -> {
                            if (currentIndex + 1 < products.itemCount) {
                                updateIndex(currentIndex + 1)
                            }
                            dragState.snapTo(Anchors.Center)
                        }

                        else -> {}
                    }
                }

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentProductModel.imageUrl)
                        .setHeader(USER_AGENT, userAgent)
                        .setHeader(
                            REFERER,
                            referrer
                        )
                        .build(),
                    contentDescription = currentProductModel.id,
                    modifier = modifier
                        .padding(1.dp)
                        .weight(1f)
                        .offset {
                            IntOffset(
                                x = dragState
                                    .requireOffset()
                                    .roundToInt(),
                                y = 0
                            )
                        }
                        .anchoredDraggable(state = dragState, orientation = Orientation.Horizontal)
                        .clickable { onImageClick(currentIndex) },

                    contentScale = ContentScale.FillWidth,
                    imageLoader = LocalContext.current.imageLoader.newBuilder()
                        .logger(DebugLogger())
                        .build()
                )

                if (currentIndex + 1 < products.itemCount) {
                    Icon(
                        modifier = Modifier
                            .clickable {
                                updateIndex(currentIndex + 1)
                            }
                            .align(Alignment.CenterVertically)
                            .padding(4.dp)
                            .size(24.dp),
                        imageVector = Icons.AutoMirrored.TwoTone.KeyboardArrowRight,
                        contentDescription = "Next"
                    )
                }

            }
            Column(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize(),
            ) {

                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween // Push Next to top, Filter to bottom
                ) {
                    Text(
                        text = "€ ${currentProductModel.price}",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                            .padding(4.dp)
                    )
                    Text(
                        text = "( ~ ${currentProductModel.priceTrend})",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.CenterVertically)
                    )
                    var showFilterDialog by remember { mutableStateOf(false) }
                    var currentFilters by remember { mutableStateOf(OfferFilters()) }

                    IconButton(onClick = {
                        showFilterDialog = true
                    }) {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(4.dp)
                                .size(36.dp),
                            imageVector = ImageVector.vectorResource(id =R.drawable.filter_solid),
                            contentDescription = "Filter"
                        )
                    }

                    if (showFilterDialog) {
                        FilterDialog(
                            initialFilters = currentFilters,
                            availableCountries = listOf("US", "UK", "DE", "JP"), // TODO: Replace with actual countries
                            availableLanguages = listOf("English", "Japanese", "French"), //TODO:  Replace with actual langs
                            onFiltersApplied = { newFilters ->
                                currentFilters = newFilters
                                // Trigger your filtering/sorting here
                            },
                            onDismiss = { showFilterDialog = false }
                        )
                    }
                }

                //add selling table
                if(productModel.sellOffers.isNotEmpty())
                {
                    OffersTable(productModel.sellOffers)
                }
            }
            //TODO: add a row for inventory management
        }
    }

}



