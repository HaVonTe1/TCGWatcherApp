package de.dkutzer.tcgwatcher.cards.control

import de.dkutzer.tcgwatcher.cards.entity.CardDetailsDto
import de.dkutzer.tcgwatcher.cards.entity.ProductModel
import de.dkutzer.tcgwatcher.cards.entity.SearchItem
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemDto
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemEntity


fun SearchResultItemDto.toSearchItemEntity(searchId: Long = 0) : SearchResultItemEntity {
    return SearchResultItemEntity(
        displayName = this.displayName,
        imgLink = this.imgLink,
        orgName = this.orgName,
        price = this.price,
        cmLink = this.cmLink,
        priceTrend = this.priceTrend,
        searchId = searchId.toInt(),
        lastUpdated = System.currentTimeMillis()
    )
}


fun SearchResultItemEntity.toSearchItem() : SearchItem {
    return SearchItem(
        displayName = this.displayName,
        orgName = this.orgName,
        cmLink = this.cmLink,
        imgLink = this.imgLink,
        price = this.price,
        timestamp = this.lastUpdated

    )
}


fun SearchResultItemEntity.toModel() : ProductModel {
    return ProductModel(
        id = android.net.Uri.parse(this.cmLink).lastPathSegment!!,
        imageUrl = this.imgLink,
        localName = this.displayName,
        orgName = this.orgName,
        detailsUrl = this.cmLink,
        price = this.price,
        priceTrend = this.priceTrend
    )
}


fun CardDetailsDto.toSearchResultItemDto(): SearchResultItemDto {

    return SearchResultItemDto(
        displayName = this.displayName,
        orgName = this.orgName,
        cmLink = this.detailsUrl,
        imgLink = this.imageUrl,
        price = this.price,
        priceTrend = this.priceTrend)
}
