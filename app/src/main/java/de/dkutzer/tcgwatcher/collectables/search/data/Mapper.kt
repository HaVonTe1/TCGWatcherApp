package de.dkutzer.tcgwatcher.collectables.search.data


import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
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


fun CardmarketProductGallaryItemDto.toProductItemEntity(searchId: Long = 0) : ProductItemEntity {
    return ProductItemEntity(
        displayName = this.name.value,
        code = if (this.code.valid) this.code.value else "",
        imgLink = this.imgLink,
        orgName = this.name.i18n,
        language = this.name.languageCode,
        genre = this.genre,
        type = this.type,
        rarity = RarityType.OTHER.name,
        price = this.price,
        cmLink = this.cmLink,
        setName = "",
        setLink = "",
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
        searchId = searchId.toInt(),
        lastUpdated = Instant.now().epochSecond
    )
}


fun ProductItemEntity.toProductModel() : ProductModel {
    return ProductModel(
        id = URI(cmLink).path.split("/").last(),
        name = NameModel(this.displayName, this.language, this.orgName),
        code = this.code,
        type = fromString<TypeEnum>(this.type) ,
        genre = fromString<GenreType>(this.genre) ,
        rarity = fromString<RarityType>(this.rarity) ,
        set = SetModel(link = this.setLink, name = this.setName),
        detailsUrl = this.cmLink,
        imageUrl = this.imgLink,
        price = this.price,
        priceTrend = this.priceTrend,
        sellOffers = listOf(),
        timestamp = this.lastUpdated

    )
}

fun ProductModel.toProductItemEntity(searchId: Int = 0) : ProductItemEntity {
    return ProductItemEntity(
        displayName = this.name.value,
        code = this.code,
        language = this.name.languageCode,
        genre = this.genre.cmCode,
        type = this.type.cmCode,
        rarity = this.rarity.cmCode,
        imgLink = this.imageUrl,
        orgName = this.name.i18n,
        price = this.price,
        cmLink = this.detailsUrl,
        priceTrend = this.priceTrend,
        lastUpdated = this.timestamp,
        setName = this.set.name,
        setLink = this.set.link,
        searchId = searchId)
}


fun CardmarketProductDetailsDto.toProductGallaryItemDto(): CardmarketProductGallaryItemDto {

    return CardmarketProductGallaryItemDto(
        name = this.name,
        code = this.code,
        cmLink = this.detailsUrl,
        imgLink = this.imageUrl,
        price = this.price,
        genre = this.genre,
        type = this.type,
        priceTrend = this.priceTrend)
}

fun CardmarketProductDetailsDto.toProductModel(language: String): ProductModel {
    return ProductModel(
        name = this.name.toModel(),
        type = fromString<TypeEnum>(this.type) ,
        genre = fromString<GenreType>(this.genre) ,
        set = SetModel(name = this.set.name,link = this.set.link),
        rarity = fromString<RarityType>(this.rarity) ,
        code = if (this.code.valid) this.code.value else "",
        detailsUrl = this.detailsUrl,
        imageUrl = this.imageUrl,
        price = this.price,
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
        id = URI(this.detailsUrl).path.split("/").last(),
        sellOffers = this.sellOffers.map { it.toSellOfferModel(language) },
        timestamp = Instant.now().epochSecond
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


