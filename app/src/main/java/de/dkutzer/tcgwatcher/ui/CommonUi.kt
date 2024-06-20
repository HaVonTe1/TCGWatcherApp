package de.dkutzer.tcgwatcher.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.cards.boundary.SearchViewCardIconRow
import de.dkutzer.tcgwatcher.cards.entity.BaseProductModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


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

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    style: TextStyle

) {
    Text(
        text = text,
        Modifier
            .border(1.dp, Color.Transparent)
            .weight(weight, true)
            .fillMaxWidth()
            .padding(1.dp),
        style = style
    )
}

private const val referrer = "https://www.cardmarket.com/"
private const val  userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"

@Composable
fun ItemOfInterestCard(
    productModel: BaseProductModel,
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
                .padding(1.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(

                model = ImageRequest.Builder(LocalContext.current)
                    .data(productModel.imageUrl)
                    .setHeader("User-Agent", userAgent)
                    .setHeader("Referer", referrer) //TODO: cloudflare protection is kicking in without the referer
                    .build(),

                contentDescription = productModel.id,
                modifier = Modifier
                    .weight(.3f, true)
                    .padding(1.dp),
                contentScale = ContentScale.FillWidth,
                imageLoader = LocalContext.current.imageLoader.newBuilder().logger(DebugLogger()).build()
            )
            Column(
                modifier = Modifier
                    .weight(.7f, false)
                    .fillMaxWidth()
                    .padding(1.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ItemDetailsTable(
                    localName = productModel.localName,
                    price = productModel.intPrice,
                    showLastUpdated = showLastUpdated,
                    lastUpdated = OffsetDateTime.now(),
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxHeight(.9f))
                Spacer(modifier = Modifier.height(16.dp)) // doesnt work with arrangement. why?
                iconRowContent()
            }
        }
    }
}

@Composable
fun ItemDetailsTable(
    localName: String,
    price : String,
    showLastUpdated: Boolean,
    lastUpdated: OffsetDateTime,
    modifier: Modifier = Modifier) {

    // Each cell of a column must have the same weight.
    val column1Weight = .3f // 30%
    val column2Weight = .7f // 70%
    // The LazyColumn will be our table. Notice the use of the weights below
    Column(modifier = modifier.padding(1.dp))
    {
        Row(Modifier.fillMaxWidth()) {
            TableCell(
                text = stringResource(id = (R.string.nameLabel)),
                weight = column1Weight,
                style = MaterialTheme.typography.labelMedium)
            TableCell(
                text = localName,
                weight = column2Weight,
                style =  MaterialTheme.typography.headlineMedium
            )
        }

        Row(Modifier.fillMaxWidth()) {
            TableCell(
                text = stringResource(id = R.string.priceLabel),
                weight = column1Weight,
                MaterialTheme.typography.labelMedium
            )
            TableCell(
                text = price,
                weight = column2Weight,
                MaterialTheme.typography.headlineLarge
            )
        }
        if(showLastUpdated) {
            Row(Modifier.fillMaxWidth()) {
                TableCell(
                    text = stringResource(id = R.string.lastUpdateLabel),
                    weight = column1Weight,
                    MaterialTheme.typography.labelMedium
                )
                TableCell(
                    text = lastUpdated.format(
                        DateTimeFormatter.ofLocalizedDateTime(
                            FormatStyle.MEDIUM
                        )
                    ), weight = column2Weight, MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}


@Preview
@Composable
fun ItemOfInterestCardPreview() {
    ItemOfInterestCard(
        productModel = BaseProductModel(
            id = "bla",
            imageUrl = "https://product-images.s3.cardmarket.com/51/TEF/760774/760774.jpg",
            intPrice = "10,00 €",
            localName = "bbbbb",
            detailsUrl = "https://product-images.s3.cardmarket.com/51/TEF/760774/760774.jpg"
        ),
        showLastUpdated = true,
        iconRowContent = { SearchViewCardIconRow() }

    )
}


