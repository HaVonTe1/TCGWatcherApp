package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.collectables.search.data.referrer
import de.dkutzer.tcgwatcher.collectables.search.data.userAgent
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.settings.domain.SettingsModel


@Composable
fun ProductListViewItemView(
    productModel: ProductModel,
    showLastUpdated: Boolean,
    settingsModel: SettingsModel,
    iconRowContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.padding(1.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(

                model = ImageRequest.Builder(LocalContext.current)
                    .data(productModel.imageUrl)
                    .setHeader("User-Agent", userAgent)
                    .setHeader("Referer", referrer)
                    .build(),

                contentDescription = productModel.id,
                modifier = Modifier
                    .weight(.3f, true)
                    .padding(4.dp),
                contentScale = ContentScale.FillWidth,
                imageLoader = LocalContext.current.imageLoader.newBuilder().logger(DebugLogger())
                    .build()
            )

            Column(
                modifier = Modifier
                    .weight(.7f, false)
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                ProductDetailsTable(
                    productModel = productModel,
                    settingsModel = settingsModel,
                    modifier = Modifier
                    .padding(4.dp)
                    .fillMaxHeight(.9f))

                iconRowContent()
            }
        }
    }
}



@Composable
fun ProductDetailsTable(
    productModel: ProductModel,
    settingsModel: SettingsModel,
    modifier: Modifier = Modifier
) {

    Column(modifier = modifier.padding(1.dp))
    {
        Text(text = productModel.getDisplayName(settingsModel.language.localeCode),
            style = MaterialTheme.typography.headlineLarge)
        if(productModel.code.isNotBlank()) {
            Text(text = " (${productModel.code})", style = MaterialTheme.typography.bodySmall)
        }
        Text(text = productModel.price, style = MaterialTheme.typography.headlineLarge)
        if(productModel.priceTrend.isNotBlank()) {
            Text(text = " (${productModel.priceTrend})", style = MaterialTheme.typography.bodySmall)
        }
    }
}