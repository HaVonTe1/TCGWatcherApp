package de.dkutzer.tcgwatcher.products.domain.ports

import org.apache.commons.lang3.StringEscapeUtils

data class ProductDetails(
    val id : String
    val name: String,

)

data class PriceDetails(
    val current: Float,
    
)
