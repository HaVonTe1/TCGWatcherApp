package de.dkutzer.tcgwatcher.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.dkutzer.tcgwatcher.Datasource
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.models.ItemDetails
import de.dkutzer.tcgwatcher.models.ItemOfInterest
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ItemOfInterestCardView(ioiList: List<ItemOfInterest>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(ioiList) {
            ItemOfInterestCard(itemOfInterest = it)
        }
    }
}

@Composable
private fun ItemOfInterestCard(itemOfInterest: ItemOfInterest, modifier: Modifier = Modifier) {

    Card(modifier = modifier.padding(8.dp), elevation = CardDefaults.cardElevation()) {

        Row(
            modifier = Modifier
                .padding(1.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(itemOfInterest.imageResourceId),
                contentDescription = stringResource(itemOfInterest.stringResourceId),
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


                ItemDetailsTable(itemDetails = itemOfInterest.details, Modifier.padding(1.dp))
                Spacer(modifier = Modifier.height(16.dp)) // doesnt work with arrangement. why?

                Row(
                    modifier = Modifier
                        .padding(1.dp)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    IconButton(
                        modifier = Modifier.fillMaxHeight(),
                        onClick = { }
                    ) {
                        Icon(
                            Icons.TwoTone.Edit,
                            "contentDescription",
                        )
                    }
                    // Spacer(modifier = Modifier.fillMaxWidth())
                    IconButton(
                        modifier = Modifier.fillMaxHeight(),
                        onClick = { }
                    ) {
                        Icon(
                            Icons.TwoTone.Delete,
                            "contentDescription",
                        )
                    }
                }
            }

        }


    }
}

@Composable
private fun RowScope.TableCell(
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
private fun ItemDetailsTable(itemDetails: ItemDetails, modifier: Modifier = Modifier) {

    // Each cell of a column must have the same weight.
    val column1Weight = .3f // 30%
    val column2Weight = .7f // 70%
    // The LazyColumn will be our table. Notice the use of the weights below
    Column(
        modifier
    )
    {

        Row(Modifier.fillMaxWidth()) {
            TableCell(text = stringResource(id = (R.string.nameLabel)), weight = column1Weight, MaterialTheme.typography.labelMedium)
            TableCell(
                text = itemDetails.name,
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

@Preview(showBackground = true)
@Composable
fun TestItemPreview() {
    ItemOfInterestCardView(Datasource().loadMockData())

}