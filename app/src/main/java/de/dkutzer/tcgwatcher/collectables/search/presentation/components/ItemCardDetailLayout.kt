package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.collectables.search.data.REFERER
import de.dkutzer.tcgwatcher.collectables.search.data.USER_AGENT
import de.dkutzer.tcgwatcher.collectables.search.data.referrer
import de.dkutzer.tcgwatcher.collectables.search.data.userAgent
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import java.time.Instant

@Composable
fun ItemCardDetailLayout(
    productModel: ProductModel,
    modifier: Modifier = Modifier,
    onRefreshItemDetailsContent: (item: ProductModel) -> Unit,
    onImageClick: (item: ProductModel) -> Unit = {},
    onBackClick: () -> Unit = {}
) {


    val item by remember(productModel) { mutableStateOf(productModel) }

            LazyColumn(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
            ) {
                items(
                    count = 1
                ) {

                    Row(modifier = Modifier.fillMaxWidth().padding(1.dp)) {
                        Icon(
                            modifier = Modifier
                                .clickable { onBackClick() }
                                .align(Alignment.CenterVertically)
                                .padding(4.dp)
                                .size(24.dp),  // Use size for equal width/height
                            imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                            contentDescription = "Back"
                        )

                        Text(
                            text = "${item.localName} (${item.code})",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .padding(4.dp).align(Alignment.CenterVertically)
                                .weight(1f),  // Takes remaining space
                            textAlign = TextAlign.Center,  // Centers text within allocated space
                            maxLines = 1,  // Prevents line breaks
                            overflow = TextOverflow.Ellipsis  // Adds "..." when text overflows
                        )
                        Icon(
                            modifier = Modifier
                                .clickable { onRefreshItemDetailsContent(item) }
                                .align(Alignment.CenterVertically)
                                .padding(4.dp)
                                .size(24.dp),  // Use size for equal width/height
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Back"
                        )
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .setHeader(USER_AGENT, userAgent)
                            .setHeader(
                                REFERER,
                                referrer
                            )
                            .build(),
                        contentDescription = item.id,
                        modifier = modifier
                            .padding(1.dp)
                            .fillMaxWidth()
                            .aspectRatio(16/14F)
                            .clickable { onImageClick(item) },

                        contentScale = ContentScale.FillHeight,
                        imageLoader = LocalContext.current.imageLoader.newBuilder()
                            .logger(DebugLogger())
                            .build()
                    )
                    Card(
                        modifier = Modifier
                            .padding(1.dp)
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation()
                    ) {
                        IconWithText(
                            painterResource(R.drawable.globe_line_icon),
                            stringResource(id = R.string.nameLabel),
                            item.orgName,
                            MaterialTheme.typography.bodyMedium
                        )
                        IconWithText(
                            painterResource(R.drawable.price_tag_euro_icon),
                            stringResource(id = R.string.priceLabel),
                            item.price,
                            MaterialTheme.typography.headlineLarge
                        )
                        IconWithText(
                            painterResource(R.drawable.stock_market_icon),
                            stringResource(id = R.string.priceLabel),
                            item.priceTrend,
                            MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
       // }
   // )
}

@Composable
@PreviewLightDark()
@Preview(name = "Light",showBackground=true)
fun ItemCardDetailLayoutPreview(modifier: Modifier = Modifier) {
    TCGWatcherTheme {
        ItemCardDetailLayout(
            productModel = ProductModel(
                "test",
                "Blitza",
                "blitza-1234",
                "Jolteon",
                "https://havonte.ddns.net/core/img/logo/logo.svg",
                "https://havonte.ddns.net/core/img/logo/logo.svg",
                "12.34",
                "56.78",
                Instant.now().epochSecond,
            ),
            onRefreshItemDetailsContent = {}
        )
    }
}