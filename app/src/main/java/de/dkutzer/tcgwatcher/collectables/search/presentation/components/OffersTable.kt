package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.dkutzer.tcgwatcher.collectables.search.domain.ConditionType
import de.dkutzer.tcgwatcher.collectables.search.domain.LanguageModel
import de.dkutzer.tcgwatcher.collectables.search.domain.LocationModel
import de.dkutzer.tcgwatcher.collectables.search.domain.OfferFilters
import de.dkutzer.tcgwatcher.collectables.search.domain.SellOfferModel
import de.dkutzer.tcgwatcher.collectables.search.domain.SpecialType
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Composable
fun OffersTable(offers: List<SellOfferModel>, filter: OfferFilters) {


    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        logger.debug { "OffersTable::recompose" }
        offers.sortedWith { o1, o2 ->
            filter.sorter(o1, o2)
        }.forEach { offer ->
            if (filter.filter(offer))
                OfferRow(offer = offer)
        }
    }
}

@Composable
private fun OfferRow(offer: SellOfferModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Seller Name
        TableCell(text = "${countryCodeToFlag(offer.sellerLocation.code)} ${offer.sellerName}", weight = 2f, maxLines = 1)

        // Language Flag
        TableCell(text = countryCodeToFlag(offer.productLanguage.code), weight = 1f)

        // Condition
        // TODO: replace with an icon or color
        TableCell(text = offer.condition.cmCode, weight = 1.5f)

        // Price
        TableCell(text = offer.price, weight = 1f)
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
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = MaterialTheme.shapes.small
            )
            .weight(weight)
            .padding(4.dp),
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
    val mappedCode = when (countryCode) {
        "en" -> "gb"
        "ja" -> "jp"
        else -> countryCode
    }
    val countryCodeUpper = mappedCode.uppercase()
    val firstChar = countryCodeUpper[0].code - 'A'.code + 0x1F1E6
    val secondChar = countryCodeUpper[1].code - 'A'.code + 0x1F1E6
    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}



@Preview(showBackground = true)
@Composable
fun OffersTablePreview() {

    TCGWatcherTheme {
        val sampleOffers = listOf(
            SellOfferModel(
                sellerName = "Card Kingdom dfsdbfkshfkshfkdshfkdshfkdhfgkhfksdhfksfhkdhfk",
                sellerLocation = LocationModel("Deutschland", "de"),
                productLanguage = LanguageModel("gb", "Deutsch"),
                condition = ConditionType.MINT,
                amount = 1,
                price = "24.99",
                special = SpecialType.REVERSED
            ),
            SellOfferModel(
                sellerName = "Card Kingdom",
                sellerLocation = LocationModel("Germany", "fr"),
                productLanguage = LanguageModel("be", "Deutsch"),
                condition = ConditionType.MINT,
                amount = 1,
                price = "24.99",
                special = SpecialType.REVERSED
            ),
            SellOfferModel(
                sellerName = "Card Kingdom",
                sellerLocation = LocationModel("Korea", "kr"),
                productLanguage = LanguageModel("en", "Englisch"),
                condition = ConditionType.MINT,
                amount = 1,
                price = "24.99",
                special = SpecialType.REVERSED
            ),
            SellOfferModel(
                sellerName = "Card Kingdom",
                sellerLocation = LocationModel("austria", "at"),
                productLanguage = LanguageModel("en", "Englisch"),
                condition = ConditionType.MINT,
                amount = 1,
                price = "24.99",
                special = SpecialType.REVERSED
            ),
            SellOfferModel(
                sellerName = "Card Kingdom",
                sellerLocation = LocationModel("Österreich", "at"),
                productLanguage = LanguageModel("ja", "Japanisch"),
                condition = ConditionType.MINT,
                amount = 1,
                price = "24.99",
                special = SpecialType.REVERSED
            ),
        )

        OffersTable(offers = sampleOffers, filter = OfferFilters())

    }
}