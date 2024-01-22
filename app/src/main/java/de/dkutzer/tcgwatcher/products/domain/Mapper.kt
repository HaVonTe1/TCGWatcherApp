package de.dkutzer.tcgwatcher.products.domain


fun SearchResultItemDto.toSearchItemEntity(searchId: Long = 0) : SearchResultItemEntity {
    return SearchResultItemEntity(
        displayName = this.displayName,
        imgLink = this.imgLink,
        orgName = this.orgName,
        price = this.price,
        cmLink = this.cmLink,
        searchId = searchId.toInt()
    )
}


fun SearchResultItemEntity.toSearchItem() : SearchItem {
    return SearchItem(
        displayName = this.displayName,
        orgName = this.orgName,
        cmLink = this.cmLink,
        imgLink = this.imgLink,
        price = this.price

    )
}


fun SearchResultItemEntity.toModel() : SearchProductModel {
    return SearchProductModel(
        id = android.net.Uri.parse(this.cmLink).lastPathSegment!!,
        imageUrl = this.imgLink,
        localName = this.displayName,
        detailsUrl = this.cmLink,
        intPrice = this.price
    )
}
