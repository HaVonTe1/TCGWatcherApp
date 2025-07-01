package de.dkutzer.tcgwatcher.collectables.search.data


import de.dkutzer.tcgwatcher.collectables.history.domain.ProductEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductComposite
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductAggregate
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductNameEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductSetEntity
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
//        displayName = this.name.value,
        code = if (this.code.valid) this.code.value else "",
        imgLink = this.imgLink,
        language = this.name.languageCode,
        genre = this.genre,
        type = this.type,
        rarity = RarityType.OTHER.name,
        price = this.price,
        externalId = this.cmId,
        externalLink = this.cmLink,
//        setName = "",
//        setId = "",
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
//        searchId = searchId.toInt(),
        id = productId,
        lastUpdated = Instant.now().epochSecond
    )
}


fun ProductComposite.toProductModel(): ProductModel {
    // Convert ProductNameEntity list to NameModel list
    val nameModels = names.map { NameModel(it.name, it.language) }

    return ProductModel(
        id = productEntity.id.toString(),
        names = nameModels, // Now using the list of names
        code = productEntity.code,
        type = fromString<TypeEnum>(productEntity.type),
        genre = fromString<GenreType>(productEntity.genre),
        rarity = fromString<RarityType>(productEntity.rarity),
        set = SetModel(
            link = set?.setId ?: "",
            name = set?.setName ?: ""
        ),
        detailsUrl = productEntity.externalLink,
        externalId = productEntity.externalId,
        imageUrl = productEntity.imgLink,
        price = productEntity.price,
        priceTrend = productEntity.priceTrend,
        sellOffers = listOf(),
        timestamp = productEntity.lastUpdated
    )
}

fun ProductAggregate.toProductModel(language: String): ProductModel {
    // Convert ProductNameEntity list to NameModel list
    val nameModels = names.map { NameModel(it.name, it.language) }

    return ProductModel(
        id = URI(productEntity.externalLink).path.split("/").last(),
        names = nameModels,
        code = productEntity.code,
        type = fromString<TypeEnum>(productEntity.type),
        genre = fromString<GenreType>(productEntity.genre),
        rarity = fromString<RarityType>(productEntity.rarity),
        set = SetModel(link = set?.setId ?: "", name = set?.setName ?: ""), // Set info no longer in ProductEntity
        externalId = productEntity.externalId,
        detailsUrl = productEntity.externalLink,
        imageUrl = productEntity.imgLink,
        price = productEntity.price,
        priceTrend = productEntity.priceTrend,
        sellOffers = offers.map { it.toSellOfferModel(language) },
        timestamp = productEntity.lastUpdated
    )
}

private fun ProductModel.toProductItemEntity(searchId: Int = 0, productId: Int = 0) : ProductEntity {
    return ProductEntity(
        code = this.code,
        language = this.primaryName.languageCode,
        genre = this.genre.cmCode,
        type = this.type.cmCode,
        rarity = this.rarity.cmCode,
        imgLink = this.imageUrl,
        price = this.price,
        externalLink = this.detailsUrl,
        externalId = this.externalId,
        priceTrend = this.priceTrend,
        lastUpdated = this.timestamp,
        id = productId
    )
}

fun ProductModel.toProductWithSellofferEntity(searchId: Int = 0, productId: Int = 0) : ProductAggregate {
    return ProductAggregate(
        productEntity = this.toProductItemEntity(searchId, productId),
        offers = this.sellOffers.map { it.toSellOfferEntity(productId) },
        names = this.names.map { ProductNameEntity(name = it.value, language = it.languageCode, productId = productId) },
        set = ProductSetEntity(productId = productId, setName = this.set.name, setId = this.set.link, language = this.primaryName.languageCode)
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

// Updated method to create ProductModel from DTO with multiple names
fun CardmarketProductDetailsDto.toProductModel(language: String): ProductModel {
    return ProductModel(
        names = listOf(this.name.toModel()), // Single name from DTO, but stored in list
        type = fromString<TypeEnum>(this.type),
        genre = fromString<GenreType>(this.genre),
        set = SetModel(name = this.set.name, link = this.set.link),
        rarity = fromString<RarityType>(this.rarity),
        code = if (this.code.valid) this.code.value else "",
        externalId = this.cmId,
        detailsUrl = this.detailsUrl,
        imageUrl = this.imageUrl,
        price = this.price,
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
        id = URI(this.detailsUrl).path.split("/").last(),
        sellOffers = this.sellOffers.map { it.toSellOfferModel(language) },
        timestamp = Instant.now().epochSecond
    )
}

//private fun CardmarketProductDetailsDto.toProductItemEntity(searchId: Long = 0, productId: Int = 0) : ProductEntity {
//    return ProductEntity(
//       // displayName = this.name.value,
//        imgLink = this.imageUrl,
//        //??
//        language = this.name.languageCode,
//        genre = this.genre,
//        type = this.type,
//        rarity = this.rarity,
//        price = this.price,
//        externalId = this.cmId,
//        externalLink = this.detailsUrl,
//        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
//        searchId = searchId.toInt(),
//        id = productId,
//        lastUpdated = Instant.now().epochSecond,
//        setName = this.set.name,
//        setId = this.set.link,
//        code = if (this.code.valid) this.code.value else "",
//    )
//}

fun CardmarketSellOfferDto.toSellOfferEntity(productId: Int, language: String): SellOfferEntity {
    return SellOfferEntity(
        productId = productId,
        sellerName = this.sellerName,
        sellerLocation = LocationModel.fromSellerLocation(this.sellerLocation, language).code,
        productLanguage = LanguageModel.fromProductLanguage(this.productLanguage, language).code,
        condition = this.condition,
        amount = this.amount.toInt(),
        price = this.price,
        special = this.special
    )
}

fun CardmarketProductDetailsDto.toProduct(language: String, searchId: Long = 0, productId: Int = 0) : ProductAggregate {
    return ProductAggregate(
        productEntity = this.toProductItemEntity(searchId, productId),
        offers = this.sellOffers.map { it.toSellOfferEntity(productId, language) }
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
    return NameModel(this.value, this.languageCode)
}