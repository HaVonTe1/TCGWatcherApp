package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
) {
    val innerPadding = 1.dp

    val item by rememberSaveable (productModel) { mutableStateOf(productModel) }

    PullToRefreshLazyColumn(
        modifier = modifier,
        onRefreshContent = { onRefreshItemDetailsContent(item) },
        content = {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
            ) {
                items(
                    count = 1
                ) {

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
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth(),

                        contentScale = ContentScale.FillWidth,
                        imageLoader = LocalContext.current.imageLoader.newBuilder()
                            .logger(DebugLogger())
                            .build()
                    )

                    Card(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        elevation = CardDefaults.cardElevation()
                    ) {
                        IconWithText(
                            painterResource(R.drawable.de_language_icon),
                            stringResource(id = R.string.nameLabel),
                            item.localName,
                            MaterialTheme.typography.headlineLarge
                        )
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
        }
    )
}

@Composable
@PreviewLightDark
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
                Instant.now().epochSecond,),
            onRefreshItemDetailsContent = {}
        )
    }
}