package de.dkutzer.tcgwatcher.cards.entity

data class SearchResultsPageDto(
    val results: List<SearchResultItemDto>,
    val page: Int,
    val totalPages: Int
)

data class SearchResultItemDto(
    val displayName: String,
    val orgName: String,
    val cmLink: String,
    val imgLink: String,
    val price: String
)

data class CardDetailsDto(
    val imageUrl: String,
    val localPrice: String,
    val localPriceTrend: String
)