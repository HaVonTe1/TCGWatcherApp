package de.dkutzer.tcgwatcher.collectables.search.data


import android.net.Uri
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductGallaryItemDto
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType.MAGIC
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType.POKEMON
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType.YUGIOH
import de.dkutzer.tcgwatcher.collectables.search.domain.NameDto
import de.dkutzer.tcgwatcher.collectables.search.domain.NameModel
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.RarityType
import de.dkutzer.tcgwatcher.collectables.search.domain.SetModel
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum.BLISTER
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum.BOOSTER
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum.BOX_SET
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum.CARD
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum.DISPLAY
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum.ELITE_TRAINER_BOX
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum.OTHER
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum.THEME_DECK
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum.TIN
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum.TRAINER_KIT
import java.time.Instant


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
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
        searchId = searchId.toInt(),
        lastUpdated = Instant.now().epochSecond
    )
}


fun ProductItemEntity.toProductModel() : ProductModel {
    return ProductModel(
        id = Uri.parse(this.cmLink).lastPathSegment!!,
        name = NameModel(this.displayName, this.language, this.orgName),
        code = this.code,
        type = TypeEnum.fromString(this.type),
        genre = GenreType.fromString(this.genre),
        rarity = RarityType.fromString(this.rarity),
        set = SetModel(this.genre, ""),
        detailsUrl = this.cmLink,
        imageUrl = this.imgLink,
        price = this.price,
        priceTrend = this.priceTrend,
        timestamp = this.lastUpdated

    )
}

fun ProductModel.toProductItemEntity(searchId: Int = 0) : ProductItemEntity {
    return ProductItemEntity(
        displayName = this.name.value,
        code = this.code,
        language = this.name.languageCode,
        genre = this.genre.name,
        type = this.type.name,
        rarity = this.rarity.name,
        imgLink = this.imageUrl,
        orgName = this.name.i18n,
        price = this.price,
        cmLink = this.detailsUrl,
        priceTrend = this.priceTrend,
        lastUpdated = this.timestamp,
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

fun CardmarketProductDetailsDto.toProductModel(): ProductModel {
    return ProductModel(
        name = this.name.toModel(),
        type = TypeEnum.fromString(this.type),
        genre = GenreType.fromString(this.genre),
        set = SetModel(this.set.name, this.set.link),
        rarity = RarityType.fromString(this.rarity),
        code = if (this.code.valid) this.code.value else "",
        detailsUrl = this.detailsUrl,
        imageUrl = this.imageUrl,
        price = this.price,
        priceTrend = if (this.priceTrend.valid) this.priceTrend.value else "",
        id = Uri.parse(this.detailsUrl).lastPathSegment!!,
        timestamp = Instant.now().epochSecond
    )
}


fun TypeEnum.Companion.fromString(value : String): TypeEnum {
    return when (value) {
        "Card" -> CARD
        "Booster" -> BOOSTER
        "Display" -> DISPLAY
        "Theme Deck" -> THEME_DECK
        "Trainer Kit" -> TRAINER_KIT
        "Tin" -> TIN
        "Box Set" -> BOX_SET
        "Elite Trainer Box" -> ELITE_TRAINER_BOX
        "Blister" -> BLISTER
        else -> OTHER
    }

}
fun GenreType.Companion.fromString(value: String): GenreType {
    return when (value) {
        "Pokemon" -> POKEMON
        "Magic" -> MAGIC
        "YuGiOh" -> YUGIOH
        else -> GenreType.OTHER
    }

}

fun RarityType.Companion.fromString(value: String): RarityType {
    return when (value) {
        "Common" -> RarityType.COMMON
        "Uncommon" -> RarityType.UNCOMMON
        "Rare" -> RarityType.RARE

        "Double Rare" -> RarityType.DOUBLE_RARE
        "Secret Rare" -> RarityType.SECRET_RARE
        "Illustration Rare" -> RarityType.ILLUSTRATION_RARE
        "Special Illustration Rare" -> RarityType.SPECIAL_ILLUSTRATION_RARE
        "Promo" -> RarityType.PROMO
        "Fixed" -> RarityType.FIXED
        "Ultra Rare" -> RarityType.ULTRA_RARE
        else -> RarityType.OTHER
    }

}



private fun NameDto.toModel(): NameModel {
    return NameModel(this.value, this.languageCode, this.i18n)
}


