package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.dkutzer.tcgwatcher.R
import java.time.OffsetDateTime

@Composable
fun ItemDetailsTable(
    localName: String,
    code: String,
    price : String,
    priceTrend: String,
    showLastUpdated: Boolean,
    lastUpdated: OffsetDateTime,
    modifier: Modifier = Modifier
) {

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