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
    val genre: GenreType,
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
): Parcelable

enum class GenreType  {
    POKEMON, MAGIC, YUGIOH, OTHER;


    override fun toString() : String {
            return when (this) {
                POKEMON -> "Pokemon"
                MAGIC -> "Magic the Gathering"
                YUGIOH -> "Yu-Gi-Oh!"
                else -> "Other"
            }
        }

    companion object
}

enum class RarityType  {
    COMMON, UNCOMMON, RARE, DOUBLE_RARE, SECRET_RARE, ILLUSTRATION_RARE,SPECIAL_ILLUSTRATION_RARE, PROMO, FIXED, ULTRA_RARE, OTHER;


    override fun toString(): String {
        return when (this) {
            COMMON -> "Common"
            UNCOMMON -> "Uncommon"
            RARE -> "Rare"
            DOUBLE_RARE -> "Double Rare"
            SECRET_RARE -> "Secret Rare"
            ILLUSTRATION_RARE -> "Illustration Rare"
            SPECIAL_ILLUSTRATION_RARE -> "Special Illustration Rare"
            PROMO -> "Promo"
            FIXED -> "Fixed"
            ULTRA_RARE -> "Ultra Rare"
            else -> "Other"
        }

    }

    companion object
}

enum class TypeEnum  {
    CARD, BOOSTER, DISPLAY,THEME_DECK, TRAINER_KIT, TIN, BOX_SET, ELITE_TRAINER_BOX, BLISTER, OTHER;

    override fun toString(): String {

        return when (this) {
            CARD -> "Card"
            BOOSTER -> "Booster"
            DISPLAY -> "Display"
            THEME_DECK -> "Theme Deck"
            TRAINER_KIT -> "Trainer Kit"
            TIN -> "TIN"
            BOX_SET -> "Box Set"
            ELITE_TRAINER_BOX -> "Elite Trainer Box"
            BLISTER -> "Blister"
            else -> "Other"
        }
    }

    companion object
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
            type = TypeEnum.CARD,
            rarity = RarityType.OTHER,
            set = SetModel(cmSetId, ""),
            genre = GenreType.OTHER,
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