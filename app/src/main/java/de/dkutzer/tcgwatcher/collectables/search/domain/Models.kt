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
    val link: String,
    val name: String,
): Parcelable

interface KeyedEnum {
    val cmCode: String

}

enum class GenreType(override val cmCode: String, val displayName: String) : KeyedEnum  {
    POKEMON("Pokemon", "Pokemon"),
    MAGIC("Magic", "Magic the Gathering"),
    YUGIOH("YuGiOh", "Yu-Gi-Oh!"),
    OTHER("","Other");

}

enum class RarityType(override val cmCode: String, val displayName: String) : KeyedEnum  {
    COMMON("Common", "Common"),
    UNCOMMON("Uncommon", "Uncommon"),
    RARE("Rare", "Rare"),
    DOUBLE_RARE("Double Rare", "Double Rare"),
    SECRET_RARE("Secret Rare", "Secret Rare"),
    ILLUSTRATION_RARE("Illustration Rare", "Illustration Rare"),
    SPECIAL_ILLUSTRATION_RARE("Special Illustration Rare", "Special Illustration Rare"),
    PROMO("Promo", "Promo"),
    FIXED("Fixed", "Fixed"),
    ULTRA_RARE("Ultra Rare", "Ultra Rare"),
    OTHER("","Other");

}

enum class TypeEnum(override val cmCode: String, val displayName: String) : KeyedEnum  {
    CARD("Singles","Card"),
    BOOSTER("Boosters","Booster"),
    DISPLAY("Booster-Boxes","Booster Display"),
    THEME_DECK("Theme-Decks","Theme Deck"),
    TRAINER_KIT("Trainer-Kits","Trainer Kit"),
    TIN("Tins","Tin"),
    BOX_SET("Box-Sets","Box Set"),
    ELITE_TRAINER_BOX("Elite-Trainer-Boxes","Elite Trainer Box"),
    BLISTER("Blisters","Blister"),
    OTHER("","Other");

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