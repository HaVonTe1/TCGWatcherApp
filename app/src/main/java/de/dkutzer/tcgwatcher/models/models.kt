package de.dkutzer.tcgwatcher.models

import android.icu.util.CurrencyAmount
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.time.OffsetDateTime

data class ItemOfInterest(
    val id : Long,
    @StringRes val stringResourceId: Int,
    @DrawableRes val imageResourceId: Int,
    val details: ItemDetails
)

data class ItemDetails(
    val name: String,
    val price: CurrencyAmount,
    val lastUpdate: OffsetDateTime
)