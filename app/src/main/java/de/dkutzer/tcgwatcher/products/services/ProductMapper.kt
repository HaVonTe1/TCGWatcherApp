package de.dkutzer.tcgwatcher.products.services

import de.dkutzer.tcgwatcher.products.config.BaseConfig
import de.dkutzer.tcgwatcher.products.domain.model.ProductDetails
import de.dkutzer.tcgwatcher.products.domain.model.ProductModel
import de.dkutzer.tcgwatcher.products.domain.model.SearchItem
import java.time.OffsetDateTime

class ProductMapper(val config: BaseConfig) {
    fun toModel(item: SearchItem, imageUrl: String) : ProductModel {

    //todo: a lot of bugs are here
        return ProductModel(
            id = android.net.Uri.parse(item.cmLink).lastPathSegment!!,
            imageUrl = imageUrl,
            detailsUrl = "${config.baseUrl}${item.cmLink}",
            details = ProductDetails(
                price = item.price,
                localName = item.displayName,
                intName = item.orgName,
                lastUpdate = OffsetDateTime.now()
            )
        )
    }
}