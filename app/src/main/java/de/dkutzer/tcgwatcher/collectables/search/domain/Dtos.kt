package de.dkutzer.tcgwatcher.collectables.search.domain

data class SearchResultsPageDto(
    val results: List<SearchResultItemDto>,
    val page: Int,
    val totalPages: Int
)

data class SearchResultItemDto(
    val displayName: String,
    val code: String,
    val orgName: String,
    val cmLink: String,
    val imgLink: String,
    val price: String,
    val priceTrend: String
)

data class CardDetailsDto(
    val displayName: String,
    val code: String,
    val orgName: String,
    val imageUrl: String,
    val detailsUrl: String,
    val price: String,
    val priceTrend: String
)