package de.dkutzer.tcgwatcher.products.adapter.port

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import de.dkutzer.tcgwatcher.products.adapter.api.SearchResultItemDto
import de.dkutzer.tcgwatcher.products.adapter.api.SearchResultsPageDto
import de.dkutzer.tcgwatcher.products.domain.model.SearchItem
import java.util.Locale


fun SearchResultItemDto.toSearchItem() : SearchItem {
    val currencyAmount =
        CurrencyAmount(this.price.toDouble(), Currency.getInstance(Locale.GERMAN))
    return SearchItem(displayName, orgName, cmLink, currencyAmount)
}
