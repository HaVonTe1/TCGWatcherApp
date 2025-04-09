package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import java.time.OffsetDateTime

@Composable
fun ItemDetailsTable(
    productModel: ProductModel,
    modifier: Modifier = Modifier
) {

    Column(modifier = modifier.padding(1.dp))
    {
        Text(text = productModel.localName, style = MaterialTheme.typography.headlineLarge)
        if(productModel.code.isNotBlank()) {
            Text(text = " (${productModel.code})", style = MaterialTheme.typography.bodySmall)
        }
        Text(text = productModel.price, style = MaterialTheme.typography.headlineLarge)
        if(productModel.priceTrend.isNotBlank()) {
            Text(text = " (${productModel.priceTrend})", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@PreviewLightDark
@Composable
fun ItemDetailsTablePreview() {
    TCGWatcherTheme {
        ItemDetailsTable(
            ProductModel(
                id = "test",
                localName = "Blitza",
                code = "1234",
                price = "12.34",
                priceTrend = "56.78",
                imageUrl = "",
                detailsUrl = "",
                timestamp = OffsetDateTime.now().toEpochSecond(),
                orgName = "kein ahnung"
            )
        )

    }
}