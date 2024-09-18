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

data class QuickSearchItem(
    val id: String,
    override  val displayName: String,
    val nameDe: String,
    val nameEn: String,
    val nameFr: String,
    val code: String
): SearchSuggestionItem(displayName)

data class HistorySearchItem(
    override val displayName: String,
): SearchSuggestionItem(displayName)

open class SearchSuggestionItem(open val displayName: String)


data class RefreshWrapper(
    val item: ProductModel? = null,
    val state: RefreshState = RefreshState.IDLE,
    val query: String
)

enum class RefreshState {
    REFRESH_ITEM, REFRESH_SEARCH, ERROR, IDLE
}
