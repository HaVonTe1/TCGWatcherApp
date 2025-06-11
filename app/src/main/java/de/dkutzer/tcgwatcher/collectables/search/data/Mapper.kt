package de.dkutzer.tcgwatcher.collectables.search.data


import de.dkutzer.tcgwatcher.collectables.history.domain.ProductEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.SellOfferEntity
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductGallaryItemDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketSellOfferDto
import de.dkutzer.tcgwatcher.collectables.search.domain.ConditionType
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType
import de.dkutzer.tcgwatcher.collectables.search.domain.KeyedEnum
import de.dkutzer.tcgwatcher.collectables.search.domain.LanguageModel
import de.dkutzer.tcgwatcher.collectables.search.domain.LocationModel
import de.dkutzer.tcgwatcher.collectables.search.domain.NameDto
import de.dkutzer.tcgwatcher.collectables.search.domain.NameModel
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.RarityType
import de.dkutzer.tcgwatcher.collectables.search.domain.SellOfferModel
import de.dkutzer.tcgwatcher.collectables.search.domain.SetModel
import de.dkutzer.tcgwatcher.collectables.search.domain.SpecialType
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum
import java.net.URI
import java.time.Instant


inline fun <reified T> fromString(value: String): T
        where T : Enum<T>, T : KeyedEnum
{
    val enumValues = enumValues<T>()
    return enumValues
        .firstOrNull { it.cmCode.equals(value, ignoreCase = true) }
        ?: enumValues.first { it.cmCode == "" } // Fallback to "other"
}


fun CardmarketProductGallaryItemDto.toProductItemEntity(searchId: Long = 0, productId: Int = 0) : ProductEntity {
    return ProductEntity(
        displayName = this.name.value,
        code = if (this.code.valid) this.code.value else "",
        imgLink = this.imgLink,
        orgName = this.name.i18n,
        language = this.name.languageCode,
        genre = this.genre,
        type = this.type,
        rarity = RarityType.OTHER.name,
        price = this.price,
        externalId = this.cmId,
        externalLink = this.cmLink,
        setName = "",
        setId = "",
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
        searchId = searchId.toInt(),
        id = productId,
        lastUpdated = Instant.now().epochSecond
    )
}


fun ProductEntity.toProductModel() : ProductModel {
    return ProductModel(
        id = URI(externalLink).path.split("/").last(),
        name = NameModel(this.displayName, this.language, this.orgName),
        code = this.code,
        type = fromString<TypeEnum>(this.type) ,
        genre = fromString<GenreType>(this.genre) ,
        rarity = fromString<RarityType>(this.rarity) ,
        set = SetModel(link = this.setId, name = this.setName),
        detailsUrl = this.externalLink,
        externalId = this.externalId,
        imageUrl = this.imgLink,
        price = this.price,
        priceTrend = this.priceTrend,
        sellOffers = listOf(),
        timestamp = this.lastUpdated

    )
}

fun ProductWithSellOffers.toProductModel() : ProductModel {
    return ProductModel(

        id = URI(productEntity.externalLink).path.split("/").last(),
        name = NameModel(productEntity.displayName, productEntity.language, productEntity.orgName),
        code = productEntity.code,
        type = fromString<TypeEnum>(productEntity.type) ,
        genre = fromString<GenreType>(productEntity.genre) ,
        rarity = fromString<RarityType>(productEntity.rarity) ,
        set = SetModel(link = productEntity.setId, name = productEntity.setName),
        externalId = productEntity.externalId,
        detailsUrl = productEntity.externalLink,
        imageUrl = productEntity.imgLink,
        price = productEntity.price,
        priceTrend = productEntity.priceTrend,
        sellOffers = offers.map { it.toSellOfferModel(productEntity.language) },
        timestamp = productEntity.lastUpdated
    )

}

fun ProductModel.toProductItemEntity(searchId: Int = 0, productId: Int = 0) : ProductEntity {
    return ProductEntity(
        displayName = this.name.value,
        code = this.code,
        language = this.name.languageCode,
        genre = this.genre.cmCode,
        type = this.type.cmCode,
        rarity = this.rarity.cmCode,
        imgLink = this.imageUrl,
        orgName = this.name.i18n, //FixMe
        price = this.price,
        externalLink = this.detailsUrl,
        externalId = this.externalId,
        priceTrend = this.priceTrend,
        lastUpdated = this.timestamp,
        setName = this.set.name,
        setId = this.set.link,
        id = productId,
        searchId = searchId)
}

fun ProductModel.toProductWithSellofferEntity(searchId: Int = 0, productId: Int = 0) : ProductWithSellOffers {
    return ProductWithSellOffers(
        productEntity = this.toProductItemEntity(searchId, productId),
        offers = this.sellOffers.map { it.toSellOfferEntity(productId) }
    )
}

fun SellOfferModel.toSellOfferEntity(productId: Int): SellOfferEntity {
    return SellOfferEntity(
        productId = productId,
        sellerName = this.sellerName,
        sellerLocation = this.sellerLocation.code,
        productLanguage = this.productLanguage.code,
        condition = this.condition.cmCode,
        amount = this.amount,
        price = this.price,
        special = this.special.cmCode
    )
}

fun SellOfferEntity.toSellOfferModel(language: String): SellOfferModel {
    return SellOfferModel(
        sellerName = this.sellerName,
        sellerLocation = LocationModel.fromCode(this.sellerLocation, language),
        productLanguage = LanguageModel.fromCode(this.productLanguage, language),
        special = fromString<SpecialType>(this.special),
        condition = fromString<ConditionType>(this.condition),
        amount = this.amount,
        price = this.price
    )
}



fun CardmarketProductDetailsDto.toProductGallaryItemDto(): CardmarketProductGallaryItemDto {

    return CardmarketProductGallaryItemDto(
        name = this.name,
        code = this.code,
        cmLink = this.detailsUrl,
        cmId= this.cmId,
        imgLink = this.imageUrl,
        price = this.price,
        genre = this.genre,
        type = this.type,
        priceTrend = this.priceTrend)
}

fun CardmarketProductDetailsDto.toProductModel(): ProductModel {
    return ProductModel(
        name = this.name.toModel(),
        type = fromString<TypeEnum>(this.type) ,
        genre = fromString<GenreType>(this.genre) ,
        set = SetModel(name = this.set.name,link = this.set.link),
        rarity = fromString<RarityType>(this.rarity) ,
        code = if (this.code.valid) this.code.value else "",
        externalId = this.cmId,
        detailsUrl = this.detailsUrl,
        imageUrl = this.imageUrl,
        price = this.price,
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
        id = URI(this.detailsUrl).path.split("/").last(),
        sellOffers = this.sellOffers.map { it.toSellOfferModel(this.name.languageCode) },
        timestamp = Instant.now().epochSecond
    )
}

fun CardmarketProductDetailsDto.toProductItemEntity(searchId: Long = 0, productId: Int = 0) : ProductEntity {
    return ProductEntity(
        displayName = this.name.value,
        imgLink = this.imageUrl,
        orgName = this.name.i18n, //??
        language = this.name.languageCode,
        genre = this.genre,
        type = this.type,
        rarity = this.rarity,
        price = this.price,
        externalId = this.cmId,
        externalLink = this.detailsUrl,
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
        searchId = searchId.toInt(),
        id = productId,
        lastUpdated = Instant.now().epochSecond,
        setName = this.set.name,
        setId = this.set.link,
        code = if (this.code.valid) this.code.value else "",
    )
}

fun CardmarketSellOfferDto.toSellOfferEntity(productId: Int): SellOfferEntity {
    return SellOfferEntity(
        productId = productId,
        sellerName = this.sellerName,
        sellerLocation = this.sellerLocation,
        productLanguage = this.productLanguage,
        condition = this.condition,
        amount = this.amount.toInt(),
        price = this.price,
        special = this.special
    )
}

fun CardmarketProductDetailsDto.toProduct(searchId: Long = 0, productId: Int = 0) : ProductWithSellOffers {
    return ProductWithSellOffers(
        productEntity = this.toProductItemEntity(searchId, productId),
        offers = this.sellOffers.map { it.toSellOfferEntity(productId) }
    )
}

private fun CardmarketSellOfferDto.toSellOfferModel(language: String): SellOfferModel {
    return SellOfferModel(
        sellerName = this.sellerName,
        sellerLocation = LocationModel.fromSellerLocation(this.sellerLocation, language),
        productLanguage = LanguageModel.fromProductLanguage(this.productLanguage, language),
        special = fromString<SpecialType>(this.special),
        condition = fromString<ConditionType>(this.condition),
        amount = this.amount.toInt(),
        price = this.price
    )

}


fun NameDto.toModel(): NameModel {
    return NameModel(this.value, this.languageCode, this.i18n)
}


