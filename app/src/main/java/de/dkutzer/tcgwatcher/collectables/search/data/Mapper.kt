package de.dkutzer.tcgwatcher.collectables.search.data


import android.net.Uri
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductGallaryItemDto
import java.time.Instant


fun ProductGallaryItemDto.toSearchItemEntity(searchId: Long = 0) : ProductItemEntity {
    return ProductItemEntity(
        displayName = this.displayName,
        code = if (this.code.valid) this.code.value else "",
        imgLink = this.imgLink,
        orgName = if (this.orgName.valid) this.orgName.value else "",
        price = this.price,
        cmLink = this.cmLink,
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
        searchId = searchId.toInt(),
        lastUpdated = Instant.now().epochSecond
    )
}


fun ProductItemEntity.toProductModel() : ProductModel {
    return ProductModel(
        id = Uri.parse(this.cmLink).lastPathSegment!!,
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


fun ProductDetailsDto.toSearchResultItemDto(): ProductGallaryItemDto {

    return ProductGallaryItemDto(
        displayName = this.displayName,
        code = this.code,
        orgName = this.orgName,
        cmLink = this.detailsUrl,
        imgLink = this.imageUrl,
        price = this.price,
        priceTrend = this.priceTrend)
}

fun ProductDetailsDto.toProductModel(): ProductModel {
    return ProductModel(
        localName = this.displayName,
        code = if (this.code.valid) this.code.value else "",
        orgName = if (this.orgName.valid) this.orgName.value else "",
        detailsUrl = this.detailsUrl,
        imageUrl = this.imageUrl,
        price = this.price,
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
        id = Uri.parse(this.detailsUrl).lastPathSegment!!,
        timestamp = Instant.now().epochSecond
    )
}


