package de.dkutzer.tcgwatcher.collectables.search.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.UUID


@Parcelize
data class ProductModel(
    val id: String,
    val localName: String,
    val code: String,
    val orgName: String,
    val imageUrl: String,
    val detailsUrl: String,
    val price: String,
    val priceTrend: String,
    val timestamp: Long
) : Parcelable


data class SearchResultsPage(
    val items: List<ProductModel>,
    val currentPage: Int,
    val pages: Int
)
val cmBasePath  = "/de/Pokemon/Products/Singles/"

data class QuickSearchItem(
    override val id: String,
    override val displayName: String,
    val nameDe: String,
    val nameEn: String,
    val nameFr: String,
    val code: String,
    val cmSetId: String,
    val cmCardId: String,
): SearchSuggestionItem(id, displayName) {
    fun toProductModel(): ProductModel {
        return ProductModel(
            id = id,
            localName = displayName,
            code = code,
            orgName = this.nameEn,
            imageUrl = "",
            detailsUrl = "$cmBasePath$cmSetId/$cmCardId",
            price = "",
            priceTrend = "",
            timestamp = Instant.now().epochSecond
        )
    }
}

data class HistorySearchItem(
    override val id: String = UUID.randomUUID().toString(),
    override val displayName: String,
): SearchSuggestionItem(id, displayName)

open class SearchSuggestionItem(open val  id: String, open val displayName: String)


data class RefreshWrapper(
    val item: ProductModel? = null,
    val state: RefreshState = RefreshState.IDLE,
    val query: String
)

enum class RefreshState {
    REFRESH_ITEM, REFRESH_SEARCH, ERROR, IDLE
}
