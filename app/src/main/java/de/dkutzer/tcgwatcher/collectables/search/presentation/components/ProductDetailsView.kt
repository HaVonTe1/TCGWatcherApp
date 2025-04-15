package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.automirrored.twotone.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.twotone.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.collectables.search.data.REFERER
import de.dkutzer.tcgwatcher.collectables.search.data.USER_AGENT
import de.dkutzer.tcgwatcher.collectables.search.data.referrer
import de.dkutzer.tcgwatcher.collectables.search.data.userAgent
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel

/**
 * Composable function that displays the detailed information of a product item in a card layout.
 *
 * @param product The [ProductModel] object containing the details of the product to be displayed.
 * @param modifier Optional [Modifier] for customizing the layout.
 * @param refreshProductDetails Callback function to refresh the product details.  It takes the current [ProductModel] as a parameter.
 */
@Composable
fun ProductDetailsView(
    products: LazyPagingItems<ProductModel>,
    index: Int,
    modifier: Modifier = Modifier,
    refreshProductDetails: (product: ProductModel) -> Unit,
    onImageClick: (product: ProductModel) -> Unit = {},
    onBackClick: () -> Unit = {}
) {

    val productModel = products[index]

    var currentProductModel by remember(productModel!!) { mutableStateOf(productModel) }
    var currentIndex by remember(index) { mutableIntStateOf(index) }

    val context = LocalContext.current
    val intent = remember { Intent(Intent.ACTION_VIEW, (referrer + productModel.detailsUrl).toUri()) }


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
                    text = "${currentProductModel.localName} (${currentProductModel.code})",
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
                if(currentIndex-1 >= 0) {
                    Icon(
                        modifier = Modifier
                            .clickable {
                                currentProductModel = products[currentIndex - 1]!!
                                currentIndex--
                            }
                            .align(Alignment.CenterVertically)
                            .padding(4.dp)
                            .size(24.dp),
                        imageVector = Icons.AutoMirrored.TwoTone.KeyboardArrowLeft,
                        contentDescription = "Previous"
                    )
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
                        //.aspectRatio(15 / 16F)
                        .clickable { onImageClick(currentProductModel) },

                    contentScale = ContentScale.FillWidth,
                    imageLoader = LocalContext.current.imageLoader.newBuilder()
                        .logger(DebugLogger())
                        .build()
                )

                if(currentIndex+1 < products.itemCount) {
                    Icon(
                        modifier = Modifier
                            .clickable {
                                currentProductModel = products[currentIndex+1]!!
                                currentIndex++
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
                    modifier = Modifier.padding(4.dp).fillMaxSize(),
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
                    Icon(
                        modifier = Modifier
                            .clickable { }
                            .align(Alignment.CenterVertically)
                            .padding(4.dp)
                            .size(36.dp),
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Filter"
                    )
                }

            }
            //TODO: add a row for inventory management
        }
    }

}



