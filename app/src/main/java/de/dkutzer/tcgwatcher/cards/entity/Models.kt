package de.dkutzer.tcgwatcher.cards.entity



data class ProductModel(
    val id: String,
    val localName: String,
    val orgName: String,
    val imageUrl: String,
    val detailsUrl: String,
    val price: String,
    val priceTrend: String,
    val timestamp: Long
)


data class SearchResultsPage(
    val items: List<ProductModel>,
    val currentPage: Int,
    val pages: Int
)

data class QuickSearchResultItem(
    val id: String,
    override  val displayName: String,
    val nameDe: String,
    val nameEn: String,
    val nameFr: String,
    val code: String
): SearchResultItem(displayName)

data class HistorySearchResultItem(
    override val displayName: String,
): SearchResultItem(displayName)

open class SearchResultItem(open val displayName: String)
