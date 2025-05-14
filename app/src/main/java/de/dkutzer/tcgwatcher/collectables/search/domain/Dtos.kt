package de.dkutzer.tcgwatcher.collectables.search.domain

data class SearchResultsPageDto(
    val results: List<CardmarketProductGallaryItemDto>,
    val page: Int,
    val totalPages: Int
)

data class CardmarketProductGallaryItemDto(
    val name: NameDto,
    val code: CodeType,
    val genre: String,
    val type: String,
    val cmLink: String,
    val imgLink: String,
    val price: String,
    val priceTrend: PriceTrendType
) {
    constructor(
        name: NameDto,
        code: String,
        genre: String,
        type: String,
        cmLink: String,
        imgLink: String,
        price: String,
        priceTrend: String
    ) : this(
        name = name,
        code = CodeType(code, code.isNotEmpty()),
        type = type,
        genre = genre,
        cmLink = cmLink,
        imgLink = imgLink,
        price = price,
        priceTrend = PriceTrendType(priceTrend, priceTrend.isNotEmpty())
    )
}

data class CodeType(val value: String, val valid: Boolean)
data class PriceTrendType(val value: String, val valid: Boolean)
data class NameDto(val value: String, val languageCode: String, val i18n: String = "")
data class SetDto(val name: String, val link: String)


data class CardmarketProductDetailsDto(
    val name: NameDto,
    val type: String,
    val genre: String,
    val code: CodeType,
    val imageUrl: String,
    val detailsUrl: String,
    val rarity: String = "",
    val set: SetDto = SetDto("", ""),
    val price: String = "0,00 €",
    val priceTrend: PriceTrendType = PriceTrendType("?", false),
    val sellOffers: List<CardmarketSellOfferDto> = emptyList()
){
    constructor(
        name: NameDto,
        type: String,
        genre: String,
        code: String,
        orgName: String,
        imageUrl: String,
        detailsUrl: String,
        rarity: String,
        set: SetDto,
        price: String,
        priceTrend: String,
        sellOffers: List<CardmarketSellOfferDto> = emptyList()
    ) : this(
        name = name,
        type = type,
        genre = genre,
        code = CodeType(code, code.isNotEmpty()),
        imageUrl = imageUrl,
        detailsUrl = detailsUrl,
        rarity = rarity,
        set = set,
        price = price,
        priceTrend = PriceTrendType(priceTrend, priceTrend.isNotEmpty()),
        sellOffers = sellOffers
    )
}


data class CardmarketSellOfferDto(
    val sellerName: String,
    val sellerLocation: String,
    val productLanguage: String,
    val special: String,
    val condition: String,
    val amount: String,
    val price: String,
)

