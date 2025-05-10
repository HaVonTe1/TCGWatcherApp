package de.dkutzer.tcgwatcher.collectables.search.domain

data class SearchResultsPageDto(
    val results: List<SearchResultItemDto>,
    val page: Int,
    val totalPages: Int
)

data class SearchResultItemDto(
    val displayName: String,
    val code: CodeType,
    val orgName: OrgNameType,
    val cmLink: String,
    val imgLink: String,
    val price: String,
    val priceTrend: PriceTrendType
) {
    constructor(
        displayName: String,
        code: String,
        orgName: String,
        cmLink: String,
        imgLink: String,
        price: String,
        priceTrend: String
    ) : this(
        displayName = displayName,
        code = CodeType(code, code.isNotEmpty()),
        orgName = OrgNameType(orgName, orgName.isNotEmpty()),
        cmLink = cmLink,
        imgLink = imgLink,
        price = price,
        priceTrend = PriceTrendType(priceTrend, priceTrend.isNotEmpty())
    )
}

data class OrgNameType(val value: String, val valid: Boolean)
data class CodeType(val value: String, val valid: Boolean)
data class PriceTrendType(val value: String, val valid: Boolean)


data class CardDetailsDto(
    val displayName: String,
    val code: CodeType,
    val orgName: OrgNameType,
    val imageUrl: String,
    val detailsUrl: String,
    val price: String,
    val priceTrend: PriceTrendType,
    val sellOffers: List<SellOfferDto> = emptyList()
){
    constructor(
        displayName: String,
        code: String,
        orgName: String,
        imageUrl: String,
        detailsUrl: String,
        price: String,
        priceTrend: String,
        sellOffers: List<SellOfferDto> = emptyList()
    ) : this(
        displayName = displayName,
        code = CodeType(code, code.isNotEmpty()),
        orgName = OrgNameType(orgName, orgName.isNotEmpty()),
        imageUrl = imageUrl,
        detailsUrl = detailsUrl,
        price = price,
        priceTrend = PriceTrendType(priceTrend, priceTrend.isNotEmpty()),
        sellOffers = sellOffers
    )
}

data class SellOfferDto(
    val sellerName: String,
    val sellerLocation: String,
    val productLanguage: String,
    val special: String,
    val condition: String,
    val amount: String,
    val price: String,
)