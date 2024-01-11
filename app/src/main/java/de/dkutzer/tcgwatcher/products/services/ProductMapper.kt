package de.dkutzer.tcgwatcher.products.services

import de.dkutzer.tcgwatcher.products.config.BaseConfig
import de.dkutzer.tcgwatcher.products.domain.SearchItem
import de.dkutzer.tcgwatcher.products.domain.SearchProductModel

class ProductMapper(val config: BaseConfig) {
//    fun toModel(item: SearchItem, details: ProductDetailsDto) : ProductModel {
//
//        return ProductModel(
//            id = android.net.Uri.parse(item.cmLink).lastPathSegment!!,
//            imageUrl = details.imageUrl,
//            detailsUrl = "${config.baseUrl}${item.cmLink}",
//            details = ProductDetailsModel(
//                price = item.price,
//                localName = item.displayName,
//                intName = item.orgName,
//                localPrice = details.localPrice.toCurrencyAmount(),
//                localPriceTrend = details.localPriceTrend.toCurrencyAmount(),
//                lastUpdate = OffsetDateTime.now()
//            )
//        )
//    }

    fun toModel(item: SearchItem) : SearchProductModel {
        return SearchProductModel(
            id = android.net.Uri.parse(item.cmLink).lastPathSegment!!,
            imageUrl = item.imgLink,
            localName = item.displayName,
            detailsUrl = item.cmLink,
            intPrice = item.price
        )
    }




}