package de.dkutzer.tcgwatcher.products.services

import android.icu.util.Currency
import android.icu.util.CurrencyAmount
import de.dkutzer.tcgwatcher.products.adapter.api.ProductDetailsDto
import de.dkutzer.tcgwatcher.products.adapter.port.toCurrencyAmount
import de.dkutzer.tcgwatcher.products.config.BaseConfig
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.OffsetDateTime
import java.util.Locale

class ProductMapper(val config: BaseConfig) {
    fun toModel(item: SearchItem, details: ProductDetailsDto) : ProductModel {

        return ProductModel(
            id = android.net.Uri.parse(item.cmLink).lastPathSegment!!,
            imageUrl = details.imageUrl,
            detailsUrl = "${config.baseUrl}${item.cmLink}",
            details = ProductDetailsModel(
                price = item.price,
                localName = item.displayName,
                intName = item.orgName,
                localPrice = details.localPrice.toCurrencyAmount(),
                localPriceTrend = details.localPriceTrend.toCurrencyAmount(),
                lastUpdate = OffsetDateTime.now()
            )
        )
    }


}