package de.dkutzer.tcgwatcher.products.adapter.port

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import de.dkutzer.tcgwatcher.products.adapter.api.SearchResultItemDto
import de.dkutzer.tcgwatcher.products.services.SearchItem
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale


fun SearchResultItemDto.toSearchItem() : SearchItem {
    return SearchItem(displayName, orgName, cmLink, price.toCurrencyAmount())
}

private val decimalRegEx = "[^\\d.,]".toRegex()
public fun String.toCurrencyAmount() : CurrencyAmount {
    val numberInstance = NumberFormat.getNumberInstance(Locale.getDefault())
    if(numberInstance is DecimalFormat) {
        numberInstance.isParseBigDecimal = true
    }

    val number = numberInstance.parse(this.replace(decimalRegEx, ""))

    return CurrencyAmount(number, Currency.getInstance(Locale.getDefault()))
}
