package de.dkutzer.tcgwatcher.collectables.search.domain

import android.os.Parcelable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.Locale
import java.util.UUID

private val logger = KotlinLogging.logger {}


@Parcelize
data class ProductModel(
    val id: String,
    val name: NameModel,
    val type: TypeEnum = TypeEnum.CARD,
    val genre: GenreType,
    val code: String,
    val externalId: String,
    val imageUrl: String,
    val detailsUrl: String,
    val rarity: RarityType,
    val set: SetModel,
    val price: String,
    val priceTrend: String,
    val sellOffers: List<SellOfferModel>,
    val timestamp: Long

) : Parcelable {

    fun getAvailableLanguages(): Set<LanguageModel> {
        return this.sellOffers.map(SellOfferModel::productLanguage).toSet()
    }


    fun getAvailableCountries(): Set<LocationModel> {
        return this.sellOffers.map(SellOfferModel::sellerLocation).toSet()
    }

    fun cmLink(): String {
        val base ="${this.name.languageCode}/${this.genre.cmCode}/Products/${this.type.cmCode}/"
        if(this.type == TypeEnum.CARD)
            return base + "${this.set.link}/${this.externalId}"
        return base + this.externalId

    }

    companion object
}

@Parcelize
data class SetModel(
    val link: String,
    val name: String,
): Parcelable

@Parcelize
data class SellOfferModel(
    val sellerName: String,
    val sellerLocation: LocationModel,
    val productLanguage: LanguageModel,
    val special: SpecialType,
    val condition: ConditionType,
    val amount: Int,
    val price: String,
): Parcelable

@Parcelize
data class LocationModel(
    val country: String,
    val code: String
): Parcelable {

    companion object {
        private val AVAILABLE_LOCALES: Array<Locale> by lazy { Locale.getAvailableLocales() } // Lazy initialization

        fun fromSellerLocation(
            sellerLocationString: String,
            language: String
        ): LocationModel {

            val targetDisplayLocale = when (language.lowercase()) {
                "de" -> Locale.GERMAN
                "en" -> Locale.ENGLISH
                else -> null // Handle unsupported languages
            }

            if (targetDisplayLocale == null) {
                logger.debug{"Warning: Unsupported language '$language'"}
                return LocationModel(country = "unknown", code = "")
            }

            // Find the matching locale once
            val foundLocale = AVAILABLE_LOCALES
                .firstOrNull { it.getDisplayCountry(targetDisplayLocale).lowercase() == sellerLocationString }

            return if (foundLocale != null) {
                LocationModel(
                    country = foundLocale.getDisplayCountry(targetDisplayLocale), // Use the determined display locale
                    code = foundLocale.country.lowercase()
                )
            } else {
                logger.debug{"Warning: Could not find locale for country '$sellerLocationString' in language '${targetDisplayLocale.language}'"}
                LocationModel(country = "unknown", code = "")
            }
        }

        fun fromCode(code: String, language: String): LocationModel {
            val targetDisplayLocale = when (language.lowercase()) {
                "de" -> Locale.GERMAN
                "en" -> Locale.ENGLISH
                else -> null // Handle unsupported languages
            }
            if (targetDisplayLocale == null) {
                logger.debug{"Warning: Unsupported language '$language'"}
                return LocationModel(country = "unknown", code = "")
            }
            val foundLocale = AVAILABLE_LOCALES.firstOrNull { it.country.lowercase() == code.lowercase() }
            return if (foundLocale != null) {
                LocationModel(
                    country = foundLocale.getDisplayCountry(targetDisplayLocale), // Use the determined display locale
                    code = foundLocale.country.lowercase()
                )
                } else {
                logger.debug { "Warning: Could not find locale for country '$code' in language '${targetDisplayLocale.language}'" }
                LocationModel(country = "unknown", code = "")
            }
        }
    }
}



@Parcelize
data class LanguageModel(
    val code: String,
    val displayName: String
) : Parcelable {
    companion object {
        private val AVAILABLE_LOCALES: Array<Locale> by lazy { Locale.getAvailableLocales() } // Lazy initialization

        fun fromProductLanguage(
            productLanguage: String,
            searchLanguage: String
        ): LanguageModel {
            val targetSearchLocale = when (searchLanguage.lowercase()) {
                "de" -> Locale.GERMAN
                "en" -> Locale.ENGLISH
                else -> return LanguageModel(code = "", displayName = "")
            }

            val productLanguageLower = productLanguage.lowercase()

            // Use the cached list
            val foundLocale = AVAILABLE_LOCALES.firstOrNull { locale ->
                locale.getDisplayLanguage(targetSearchLocale).lowercase() == productLanguageLower
            }

            return foundLocale?.let {
                LanguageModel(
                    code = it.language,
                    displayName = it.getDisplayLanguage(targetSearchLocale)
                )
            } ?: LanguageModel(code = "", displayName = "")
        }

        fun fromCode(code: String, language: String): LanguageModel {
            val targetSearchLocale = when (language.lowercase()) {
                "de" -> Locale.GERMAN
                "en" -> Locale.ENGLISH
                else -> return LanguageModel(code = "", displayName = "")
            }
            val foundLocale = AVAILABLE_LOCALES.firstOrNull { it.language.lowercase() == code.lowercase() }

            return foundLocale?.let {
                LanguageModel(
                    code = it.language,
                    displayName = it.getDisplayLanguage(targetSearchLocale)
                )

                } ?: LanguageModel(code = "", displayName = "")
        }
    }
}

interface KeyedEnum {
    val cmCode: String

}

enum class SpecialType(override val cmCode: String) : KeyedEnum {
    REVERSED("Reverse Holo"),OTHER("")
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
    COIN("Coins","Coin"),
    LOT("Lots","Lot"),
    ELITE_TRAINER_BOX("Elite-Trainer-Boxes","Elite Trainer Box"),
    BLISTER("Blisters","Blister"),
    OTHER("","Other");

}
enum class ConditionType(override val cmCode: String): KeyedEnum {
    MINT("Mint"),NEAR_MINT("Near Mint"), EXCELLENT("Excellent"), GOOD("Goog"), LIGHT_PLAYED("Light Played"), PLAYED("Played"), POOR("Poor"), OTHER("")
}

@Parcelize
data class NameModel(val value: String, val languageCode: String, val i18n: String): Parcelable


data class SearchResultsPage(
    val items: List<ProductModel>,
    val currentPage: Int,
    val pages: Int
)


data class QuickSearchItem(
    override val id: String,
    override val displayName: String,
    val nameDe: String,
    val nameEn: String,
    val nameFr: String,
    val code: String,
    val cmSetId: String,
    val cmCardId: String,
    //TODO: add genre as soon as more than one genre is supported
): SearchSuggestionItem(id, displayName) {
    fun toProductModel( currentLanguageCode : String = "de"): ProductModel {
        return ProductModel(
            id = id,
            name = NameModel(displayName, currentLanguageCode, this.nameEn),
            code = code,
            externalId = cmCardId,
            type = TypeEnum.CARD,
            rarity = RarityType.OTHER,
            set = SetModel(cmSetId, ""),
            genre = GenreType.POKEMON, //TODO: fix this as soon as more than one genre is supported
            imageUrl = "",
            detailsUrl = "${currentLanguageCode}/${GenreType.POKEMON.cmCode}/Products/${TypeEnum.CARD.cmCode}/${cmSetId}/${cmCardId}",
            price = "",
            priceTrend = "",
            sellOffers = emptyList(),
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
    REFRESH_ITEM, REFRESH_SEARCH,REFRESH_ITEM_FROM_CACHE, ERROR, IDLE
}


enum class SortField {
    PRICE, CONDITION, SELLER_NAME, SELLER_COUNTRY, LANGUAGE
}

enum class SortOrder {
    ASCENDING, DESCENDING
}


data class OfferFilters(
    val sellerName: String = "",
    val sellerCountries: Set<LocationModel> = emptySet(),
    val languages: Set<LanguageModel> = emptySet(),
    val conditions: Set<ConditionType> = emptySet(),
    val priceRange: ClosedFloatingPointRange<Float> = 0f..Float.MAX_VALUE,
    val sortBy: SortField = SortField.PRICE,
    val sortOrder: SortOrder = SortOrder.ASCENDING
)

