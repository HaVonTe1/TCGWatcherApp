package de.dkutzer.tcgwatcher.products.domain.model

import android.icu.util.CurrencyAmount
import java.time.OffsetDateTime

data class ProductModel(
    val id : String,
    val imageUrl : String,
    val detailsUrl : String,
    val details: ProductDetails
)

data class ProductDetails(
    val localName: String,
    val intName: String,
    val price: CurrencyAmount,
    val lastUpdate: OffsetDateTime
)

data class SearchItem(
    val displayName : String,
    val orgName: String,
    val cmLink: String,
    val price : CurrencyAmount
)
