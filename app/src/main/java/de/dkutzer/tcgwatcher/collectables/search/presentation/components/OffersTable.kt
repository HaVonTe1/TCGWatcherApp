package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme

data class Offer(
    val sellerName: String,
    val sellerLocation: String, // ISO 2-letter country code (e.g., "US")
    val language: String,       // ISO 2-letter country code (e.g., "JP" for Japanese)
    val condition: String,
    val price: Double
)

@Composable
fun OffersTable(offers: List<Offer>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Seller", 2f)
            TableHeaderCell("Location", 1f)
            TableHeaderCell("Language", 1f)
            TableHeaderCell("Condition", 1.5f)
            TableHeaderCell("Price", 1f)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        // Offers List
        LazyColumn {
            items(offers) { offer ->
                OfferRow(offer = offer)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
@Composable
private fun OfferRow(offer: Offer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Seller Name
        TableCell(text = offer.sellerName, weight = 2f, maxLines = 2)

        // Location Flag
        TableCell(text = countryCodeToFlag(offer.sellerLocation), weight = 1f)

        // Language Flag
        TableCell(text = countryCodeToFlag(offer.language), weight = 1f)

        // Condition
        TableCell(text = offer.condition.replaceFirstChar { it.uppercase() }, weight = 1.5f)

        // Price
        TableCell(text = "$${"%.2f".format(offer.price)}", weight = 1f)
    }
}


@Composable
private fun RowScope.TableHeaderCell(text: String, weight: Float) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(1.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    maxLines: Int = 1
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(1.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun countryCodeToFlag(countryCode: String): String {
    if (countryCode.length != 2) return ""
    val countryCodeUpper = countryCode.uppercase()
    val firstChar = countryCodeUpper[0].code - 'A'.code + 0x1F1E6
    val secondChar = countryCodeUpper[1].code - 'A'.code + 0x1F1E6
    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}

@PreviewLightDark
@Composable
fun OffersTablePreview() {

    TCGWatcherTheme {
        val sampleOffers = listOf(
            Offer(
                sellerName = "Card Kingdom",
                sellerLocation = "US",
                language = "EN",
                condition = "near mint",
                price = 24.99
            ),
            Offer(
                sellerName = "Tokyo Collectibles",
                sellerLocation = "JP",
                language = "JP",
                condition = "mint",
                price = 42.50
            )
        )

        OffersTable(offers = sampleOffers)

    }
}