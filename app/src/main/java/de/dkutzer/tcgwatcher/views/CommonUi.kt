package de.dkutzer.tcgwatcher.views

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.products.services.ProductDetailsModel
import de.dkutzer.tcgwatcher.products.services.ProductModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@Composable
fun ClickableIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier.fillMaxHeight(),
        onClick = onClick
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
            .weight(weight)
            .padding(1.dp),
        style = style
    )
}
@Composable
fun ItemOfInterestCard(
    productModel: ProductModel,
    showLastUpdated: Boolean,
    iconRowContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(modifier = modifier.padding(8.dp), elevation = CardDefaults.cardElevation()) {
        Row(
            modifier = Modifier
                .padding(1.dp)
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(productModel.imageUrl)
                    .setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                    .setHeader("Referer", "https://www.cardmarket.com/") //TODO: cloudflare protection is kicking in without the referer
                    .build(),
                contentDescription = productModel.details.intName,
                modifier = Modifier
                    .padding(4.dp)
                    .width(100.dp),
                contentScale = ContentScale.Fit

            )
            Column(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {


                ItemDetailsTable(
                    itemDetails = productModel.details,
                    showLastUpdated = showLastUpdated,
                    modifier = Modifier.padding(1.dp))
                Spacer(modifier = Modifier.height(16.dp)) // doesnt work with arrangement. why?
                iconRowContent()

            }
        }
    }
}

@Composable
fun ItemDetailsTable(
    itemDetails: ProductDetailsModel,
    showLastUpdated: Boolean,
    modifier: Modifier = Modifier) {

    // Each cell of a column must have the same weight.
    val column1Weight = .3f // 30%
    val column2Weight = .7f // 70%
    // The LazyColumn will be our table. Notice the use of the weights below
    Column(modifier = modifier)
    {

        Row(Modifier.fillMaxWidth()) {
            TableCell(text = stringResource(id = (R.string.nameLabel)), weight = column1Weight, MaterialTheme.typography.labelMedium)
            TableCell(
                text = itemDetails.localName,
                weight = column2Weight,
                MaterialTheme.typography.labelLarge
            )
        }

        Row(Modifier.fillMaxWidth()) {
            TableCell(
                text = stringResource(id = R.string.priceLabel),
                weight = column1Weight,
                MaterialTheme.typography.labelMedium
            )
            TableCell(
                text = itemDetails.price.toString(),
                weight = column2Weight,
                MaterialTheme.typography.labelLarge
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
                    text = itemDetails.lastUpdate.format(
                        DateTimeFormatter.ofLocalizedDateTime(
                            FormatStyle.MEDIUM
                        )
                    ), weight = column2Weight, MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}





