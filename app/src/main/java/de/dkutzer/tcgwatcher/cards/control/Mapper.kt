package de.dkutzer.tcgwatcher.cards.control

import de.dkutzer.tcgwatcher.cards.entity.CardDetailsDto
import de.dkutzer.tcgwatcher.cards.entity.ProductItemEntity
import de.dkutzer.tcgwatcher.cards.entity.ProductModel
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemDto
import java.time.Instant


fun SearchResultItemDto.toSearchItemEntity(searchId: Long = 0) : ProductItemEntity {
    return ProductItemEntity(
        displayName = this.displayName,
        code = this.code,
        imgLink = this.imgLink,
        orgName = this.orgName,
        price = this.price,
        cmLink = this.cmLink,
        priceTrend = this.priceTrend,
        searchId = searchId.toInt(),
        lastUpdated = Instant.now().epochSecond
    )
}


fun ProductItemEntity.toProductModel() : ProductModel {
    return ProductModel(
        id = android.net.Uri.parse(this.cmLink).lastPathSegment!!,
        localName = this.displayName,
        code = this.code,
        orgName = this.orgName,
        detailsUrl = this.cmLink,
        imageUrl = this.imgLink,
        price = this.price,
        priceTrend = this.priceTrend,
        timestamp = this.lastUpdated

    )
}

fun ProductModel.toSearchResultItemEntity(searchId: Int = 0) : ProductItemEntity {
    return ProductItemEntity(
        displayName = this.localName,
        code = this.code,
        imgLink = this.imageUrl,
        orgName = this.orgName,
        price = this.price,
        cmLink = this.detailsUrl,
        priceTrend = this.priceTrend,
        lastUpdated = this.timestamp,
        searchId = searchId)
}


fun CardDetailsDto.toSearchResultItemDto(): SearchResultItemDto {

    return SearchResultItemDto(
        displayName = this.displayName,
        code = this.code,
        orgName = this.orgName,
        cmLink = this.detailsUrl,
        imgLink = this.imageUrl,
        price = this.price,
        priceTrend = this.priceTrend)
}

fun CardDetailsDto.toProductModel(): ProductModel {
    return ProductModel(
        localName = this.displayName,
        code = this.code,
        orgName = this.orgName,
        detailsUrl = this.detailsUrl,
        imageUrl = this.imageUrl,
        price = this.price,
        priceTrend = this.priceTrend,
        id = android.net.Uri.parse(this.detailsUrl).lastPathSegment!!,
        timestamp = Instant.now().epochSecond
    )
}


