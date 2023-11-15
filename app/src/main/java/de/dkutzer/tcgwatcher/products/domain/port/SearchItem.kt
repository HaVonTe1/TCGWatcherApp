package de.dkutzer.tcgwatcher.products.domain.port

import android.icu.util.CurrencyAmount

data class SearchItem(
    val displayName : String,
    val orgName: String,
    val cmLink: String,
    val price : CurrencyAmount
)
