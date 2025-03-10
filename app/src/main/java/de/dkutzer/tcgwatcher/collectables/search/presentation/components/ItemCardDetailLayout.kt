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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import java.time.Instant

@Composable
fun ItemCardDetailLayout(
    productModel: ProductModel,
    modifier: Modifier = Modifier,
    onRefreshItemDetailsContent: (item: ProductModel) -> Unit,
    onImageClick: (item: ProductModel) -> Unit = {}
) {


    val item by remember(productModel) { mutableStateOf(productModel) }

    PullToRefreshLazyColumn(
        modifier = modifier,
        onRefreshContent = { onRefreshItemDetailsContent(item) },
        content = {
            LazyColumn(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
            ) {
                items(
                    count = 1
                ) {

                    CardImage(item, onImageClick)

                    Card(
                        modifier = Modifier
                            .padding(1.dp)
                            .fillMaxWidth(),
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
                Instant.now().epochSecond,
            ),
            onRefreshItemDetailsContent = {}
        )
    }
}