package de.dkutzer.tcgwatcher.products.domain.port


data class ProductDetails(
    val id : String,
    val name: String,
    val prices : PriceDetails
)

data class PriceDetails(
    val current: Float,
    val trend: Float
)
