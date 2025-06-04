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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.collectables.search.data.REFERER
import de.dkutzer.tcgwatcher.collectables.search.data.USER_AGENT
import de.dkutzer.tcgwatcher.collectables.search.data.referrer
import de.dkutzer.tcgwatcher.collectables.search.data.userAgent
import de.dkutzer.tcgwatcher.collectables.search.domain.ConditionType
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType
import de.dkutzer.tcgwatcher.collectables.search.domain.LanguageModel
import de.dkutzer.tcgwatcher.collectables.search.domain.LocationModel
import de.dkutzer.tcgwatcher.collectables.search.domain.NameModel
import de.dkutzer.tcgwatcher.collectables.search.domain.OfferFilters
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.RarityType
import de.dkutzer.tcgwatcher.collectables.search.domain.SellOfferModel
import de.dkutzer.tcgwatcher.collectables.search.domain.SetModel
import de.dkutzer.tcgwatcher.collectables.search.domain.SpecialType
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import kotlin.math.roundToInt

private enum class Anchors { Left, Center, Right }



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductDetailsView(
    modifier: Modifier = Modifier,

    initialProduct: ProductModel,
    currentIndex: Int = 0,
    nextIndex: Int? = null,
    previousIndex: Int? = null,
    onImageClick: (id: Int) -> Unit = {},
    onBackClick: () -> Unit = {},
    onChangedIndex: (id: Int) -> Unit = {},
    onLoadProduct: (productId: String, cacheOnly: Boolean) -> ProductModel = { _, _ -> initialProduct },
) {

    var currentProductModel by remember { mutableStateOf<ProductModel>(initialProduct) }

    // Load data (runs once when composable enters composition)
    LaunchedEffect(key1 = initialProduct) {
        currentProductModel = onLoadProduct(initialProduct.id, true)
    }
    val context = LocalContext.current
    val intent =
        remember { Intent(Intent.ACTION_VIEW, (referrer + currentProductModel.detailsUrl).toUri()) }

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
                        .clickable { onLoadProduct(currentProductModel.id, false) }
                        .align(Alignment.CenterVertically)
                        .padding(4.dp)
                        .size(24.dp),
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
            Row {
                if (previousIndex!=null) {
                    Icon(
                        modifier = Modifier
                            .clickable {
                                onChangedIndex(previousIndex)
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
                            if (previousIndex!=null) {
                                onChangedIndex(previousIndex)
                            }
                            dragState.snapTo(Anchors.Center)
                        }

                        Anchors.Left -> {
                            if (nextIndex!=null) {
                                onChangedIndex(nextIndex)
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

                if (nextIndex!=null) {
                    Icon(
                        modifier = Modifier
                            .clickable {
                                onChangedIndex(nextIndex)
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
                    .fillMaxWidth(),
            ) {

                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
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
                            availableCountries = currentProductModel.getAvailableCountries(),
                            availableLanguages = currentProductModel.getAvailableLanguages(),
                            onFiltersApplied = { newFilters ->
                                currentFilters = newFilters
                                // Trigger your filtering/sorting here
                            },
                            onDismiss = { showFilterDialog = false }
                        )
                    }
                }


            }
            //add selling table
            if(currentProductModel.sellOffers.isNotEmpty())
            {
                OffersTable(currentProductModel.sellOffers)
            }
            //TODO: add a row for inventory management
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ProductDetailsViewPreview() {
    val previewProducts = listOf(
        ProductModel(
            id = "prev2",
            name = NameModel("Preview Product 1", languageCode = "de", "dfdsf"),
            code = "PRV002",
            price = "29.99",
            imageUrl = "http://example.com/image2.png",
            detailsUrl = "http://example.com/details2",
            sellOffers = listOf(SellOfferModel(
                sellerName = "sdf",
                sellerLocation = LocationModel(country = "df", code = "de"),
                productLanguage = LanguageModel(code = "de", displayName = "df"),
                special = SpecialType.REVERSED,
                condition = ConditionType.NEAR_MINT,
                amount = 11,
                price = "123"
            ), SellOfferModel(
                sellerName = "sdf",
                sellerLocation = LocationModel(country = "df", code = "de"),
                productLanguage = LanguageModel(code = "de", displayName = "df"),
                special = SpecialType.REVERSED,
                condition = ConditionType.NEAR_MINT,
                amount = 11,
                price = "123"
            )),
            type = TypeEnum.CARD,
            genre = GenreType.POKEMON,
            rarity = RarityType.COMMON,
            set = SetModel(link = "df", name = "dfsdf"),
            priceTrend = "34",
            timestamp = System.currentTimeMillis()
        )
    )


    TCGWatcherTheme {
            ProductDetailsView(

                modifier = Modifier,
                initialProduct = previewProducts[0],
                currentIndex = 0,
                nextIndex = null,
                previousIndex = null,
                onLoadProduct = { _, _ -> previewProducts[0] },
                onChangedIndex = { /* No-op for preview */ },
                onImageClick = { /* No-op for preview */ },
                onBackClick = { /* No-op for preview */ },
            )

    }
}



