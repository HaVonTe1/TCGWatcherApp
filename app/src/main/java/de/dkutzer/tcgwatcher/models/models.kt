package de.dkutzer.tcgwatcher.models

import android.icu.util.CurrencyAmount
import android.media.Image
import io.ktor.http.Url
import java.time.OffsetDateTime

data class ItemOfInterest(
    val id : Long,
    val imageUrl : String,
    val detailsUrl : String,
    val details: ItemDetails
)

data class ItemDetails(
    val localName: String,
    val intName: String,
    val price: CurrencyAmount,
    val lastUpdate: OffsetDateTime
)