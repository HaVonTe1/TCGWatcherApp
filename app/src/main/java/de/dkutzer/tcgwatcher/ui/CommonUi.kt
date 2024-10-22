package de.dkutzer.tcgwatcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.compose.TCGWatcherTheme
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.presentation.components.IconWithText
import java.time.Instant
import java.time.OffsetDateTime


@Composable
fun ClickableIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    desc: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier.fillMaxHeight(),
        onClick = onClick,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
        )
    }
}

//TODO: make R resources of this
const val referrer = "https://www.cardmarket.com/"
private const val  userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"

@Composable
fun ItemOfInterestCard(
    productModel: ProductModel,
    showLastUpdated: Boolean,
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
                imageLoader = LocalContext.current.imageLoader.newBuilder().logger(DebugLogger()).build()
            )
            Column(
                modifier = Modifier
                    .weight(.7f, false)
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                ItemDetailsTable(
                    localName = productModel.localName,
                    code = productModel.code,
                    price = productModel.price,
                    priceTrend = productModel.priceTrend,
                    showLastUpdated = showLastUpdated,
                    lastUpdated = OffsetDateTime.now(),
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxHeight(.9f))
                iconRowContent()
            }
        }
    }
}

@Composable
fun ItemDetailsTable(
    localName: String,
    code: String,
    price : String,
    priceTrend: String,
    showLastUpdated: Boolean,
    lastUpdated: OffsetDateTime,
    modifier: Modifier = Modifier) {

    Column(modifier = modifier.padding(1.dp))
    {

            IconWithText(
                icon = painterResource(R.drawable.de_language_icon),
                desc = stringResource(id = R.string.nameLabel),
                text = localName,
                testStyle = MaterialTheme.typography.headlineLarge,
                iconHeigh = 32
            )

            Text(text = " ($code)", style = MaterialTheme.typography.bodySmall)

            IconWithText(
                icon = painterResource(R.drawable.price_tag_euro_icon),
                desc = stringResource(id = R.string.nameLabel),
                text = price,
                testStyle = MaterialTheme.typography.headlineLarge,
                iconHeigh = 32

            )
            Text(text = " ($priceTrend)", style = MaterialTheme.typography.bodySmall)

    }
}



@PreviewLightDark()
@Composable
fun ItemOfInterestCardPreview() {
    TCGWatcherTheme {
        val sampleProduct = ProductModel(
            "test",
            "Blitza",
            "blitza-1234",
            "Jolteon",
            "https://havonte.ddns.net/core/img/logo/logo.svg",
            "https://havonte.ddns.net/core/img/logo/logo.svg",
            "12.34",
            "56.78",
            Instant.now().epochSecond,)
        ItemOfInterestCard(productModel = sampleProduct,
            showLastUpdated = true,
            iconRowContent = {}
        )
    }
}