package de.dkutzer.tcgwatcher.collectables.search.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.UUID


@Parcelize
data class ProductModel(
    val id: String,
    val name: NameModel,
    val type: TypeEnum = TypeEnum.CARD,
    val code: String,
    val imageUrl: String,
    val detailsUrl: String,
    val rarity: RarityType,
    val set: SetModel,
    val price: String,
    val priceTrend: String,
    val timestamp: Long
) : Parcelable

@Parcelize
data class SetModel(
    val id: String,
    val name: String,
    val imageUrl: String
): Parcelable

enum class RarityType {
    COMMON, UNCOMMON, RARE, DOUBLE_RARE, SECRET_RARE, ILLUSTRATION_RARE,SPECIAL_ILLUSTRATION_RARE, PROMO, FIXED, ULTRA_RARE, OTHER

    fun fromString(value: String): RarityType {
        return when (value) {
            "Common" -> COMMON
            "Uncommon" -> UNCOMMON
            "Rare" -> RARE
            "Double Rare" -> DOUBLE_RARE
            "Secret Rare" -> SECRET_RARE
            "Illustration Rare" -> ILLUSTRATION_RARE
            "Special Illustration Rare" -> SPECIAL_ILLUSTRATION_RARE
            "Promo" -> PROMO
            "Fixed" -> FIXED
            "Ultra Rare" -> ULTRA_RARE
            else -> OTHER
        }
    }
}

enum class TypeEnum {
    CARD, BOOSTER, DISPLAY,THEME_DECK, TRAINER_KIT, TIN, BOX_SET, ELITE_TRAINER_BOX, BLISTER
}

@Parcelize
data class NameModel(val value: String, val languageCode: String, val i18n: String): Parcelable


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
            name = NameModel(displayName, "de", this.nameEn),
            code = code,
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


enum class SortField {
    PRICE, CONDITION, SELLER_NAME, SELLER_COUNTRY, LANGUAGE
}

enum class SortOrder {
    ASCENDING, DESCENDING
}

enum class Condition(val displayName: String) {
    MINT("Mint"),NEAR_MINT("Near Mint"), EXCELLENT("Excellent"), GOOD("Goog"), LIGHT_PLAYED("Light Played"), PLAYED("Played"), POOR("Poor")
}

data class OfferFilters(
    val sellerName: String = "",
    val sellerCountries: Set<String> = emptySet(),
    val languages: Set<String> = emptySet(),
    val conditions: Set<Condition> = emptySet(),
    val priceRange: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE,
    val sortBy: SortField = SortField.PRICE,
    val sortOrder: SortOrder = SortOrder.ASCENDING
)