package de.dkutzer.tcgwatcher.collectables.search.domain

import java.util.concurrent.ConcurrentHashMap

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

    companion object {
        fun empty(): ProductModel = ProductModel(
            id = 1.toString(),
            name = NameModel(value = "", languageCode = ""),
            genre = GenreType.POKEMON,
            code = "",
            imageUrl = "",
            detailsUrl = "",
            rarity = RarityType.OTHER,
            set = SetModel(link = "", name = ""),
            price = "",
            priceTrend = "",
            sellOffers = emptyList(),
            timestamp = System.currentTimeMillis(),
            type = TypeEnum.CARD,
            externalId = "bla_blub"
        )
    }

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
            val cacheKey = sellerLocationString.lowercase() to language.lowercase()
            val locationCacheBySellerLocation = Companion.locationCacheBySellerLocation
            locationCacheBySellerLocation[cacheKey]?.let { return it }

            val targetDisplayLocale = when (language.lowercase()) {
                "de" -> Locale.GERMAN
                "en" -> Locale.ENGLISH
                else -> null // Handle unsupported languages
            }

            if (targetDisplayLocale == null) {
                logger.debug{"Warning: Unsupported language '$language'"}
                val unknown = LocationModel(country = "unknown", code = "")
                locationCacheBySellerLocation[cacheKey] = unknown
                return unknown
            }

            val foundLocale = AVAILABLE_LOCALES
                .firstOrNull { it.getDisplayCountry(targetDisplayLocale).lowercase() == sellerLocationString.lowercase() }

            val result = if (foundLocale != null) {
                LocationModel(
                    country = foundLocale.getDisplayCountry(targetDisplayLocale),
                    code = foundLocale.country.lowercase()
                )
            } else {
                logger.debug{"Warning: Could not find locale for country '$sellerLocationString' in language '[${targetDisplayLocale.language}'"}
                LocationModel(country = "unknown", code = "")
            }
            locationCacheBySellerLocation[cacheKey] = result
            return result
        }

        fun fromCode(code: String, language: String): LocationModel {
            val cacheKey = code.lowercase() to language.lowercase()
            val locationCacheByCode = Companion.locationCacheByCode
            locationCacheByCode[cacheKey]?.let { return it }

            val targetDisplayLocale = when (language.lowercase()) {
                "de" -> Locale.GERMAN
                "en" -> Locale.ENGLISH
                else -> null // Handle unsupported languages
            }
            if (targetDisplayLocale == null) {
                logger.debug{"Warning: Unsupported language '$language'"}
                val unknown = LocationModel(country = "unknown", code = "")
                locationCacheByCode[cacheKey] = unknown
                return unknown
            }
            val foundLocale = AVAILABLE_LOCALES.firstOrNull { it.country.lowercase() == code.lowercase() }
            val result = if (foundLocale != null) {
                LocationModel(
                    country = foundLocale.getDisplayCountry(targetDisplayLocale),
                    code = foundLocale.country.lowercase()
                )
            } else {
                logger.debug { "Warning: Could not find locale for country '$code' in language '[${targetDisplayLocale.language}'" }
                LocationModel(country = "unknown", code = "")
            }
            locationCacheByCode[cacheKey] = result
            return result
        }

        // Caching maps in companion object for function scope
        private val locationCacheBySellerLocation = ConcurrentHashMap<Pair<String, String>, LocationModel>()
        private val locationCacheByCode = ConcurrentHashMap<Pair<String, String>, LocationModel>()
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
            val cacheKey = productLanguage.lowercase() to searchLanguage.lowercase()
            val languageCacheByProductLanguage = Companion.languageCacheByProductLanguage
            languageCacheByProductLanguage[cacheKey]?.let { return it }

            val targetSearchLocale = when (searchLanguage.lowercase()) {
                "de" -> Locale.GERMAN
                "en" -> Locale.ENGLISH
                else -> {
                    val unknown = LanguageModel(code = "", displayName = "")
                    languageCacheByProductLanguage[cacheKey] = unknown
                    return unknown
                }
            }

            val productLanguageLower = productLanguage.lowercase()
            val foundLocale = AVAILABLE_LOCALES.firstOrNull { locale ->
                locale.getDisplayLanguage(targetSearchLocale).lowercase() == productLanguageLower
            }

            val result = foundLocale?.let {
                LanguageModel(
                    code = it.language,
                    displayName = it.getDisplayLanguage(targetSearchLocale)
                )
            } ?: LanguageModel(code = "", displayName = "")
            languageCacheByProductLanguage[cacheKey] = result
            return result
        }

        fun fromCode(code: String, language: String): LanguageModel {
            val cacheKey = code.lowercase() to language.lowercase()
            val languageCacheByCode = Companion.languageCacheByCode
            languageCacheByCode[cacheKey]?.let { return it }

            val targetSearchLocale = when (language.lowercase()) {
                "de" -> Locale.GERMAN
                "en" -> Locale.ENGLISH
                else -> {
                    val unknown = LanguageModel(code = "", displayName = "")
                    languageCacheByCode[cacheKey] = unknown
                    return unknown
                }
            }
            val foundLocale = AVAILABLE_LOCALES.firstOrNull { it.language.lowercase() == code.lowercase() }
            val result = foundLocale?.let {
                LanguageModel(
                    code = it.language,
                    displayName = it.getDisplayLanguage(targetSearchLocale)
                )
            } ?: LanguageModel(code = "", displayName = "")
            languageCacheByCode[cacheKey] = result
            return result
        }

        // Caching maps in companion object for function scope
        private val languageCacheByProductLanguage = ConcurrentHashMap<Pair<String, String>, LanguageModel>()
        private val languageCacheByCode = ConcurrentHashMap<Pair<String, String>, LanguageModel>()
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
data class NameModel(val value: String, val languageCode: String): Parcelable


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
            name = NameModel(displayName, currentLanguageCode),
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
) {
    fun filter(model: SellOfferModel): Boolean {

        if(sellerCountries.isNotEmpty())
        {
            if(model.sellerLocation !in sellerCountries)
                return false
        }
        if(languages.isNotEmpty())
        {
            if(model.productLanguage !in languages)
                return false
        }
        if(conditions.isNotEmpty())
        {
            if(model.condition !in conditions)
                return false

        }
        //TODO: price

        return true
    }

    fun sorter(m1: SellOfferModel, m2: SellOfferModel): Int {

        return when (sortBy) {
            SortField.PRICE -> {
                when (sortOrder) {
                    SortOrder.ASCENDING -> m1.price.compareTo(m2.price)
                    SortOrder.DESCENDING -> m2.price.compareTo(m1.price)
                }
            }
            SortField.CONDITION -> {
                when (sortOrder) {
                    SortOrder.ASCENDING -> m1.condition.ordinal.compareTo(m2.condition.ordinal)
                    SortOrder.DESCENDING -> m2.condition.ordinal.compareTo(m1.condition.ordinal)
                }
            }
            SortField.SELLER_COUNTRY -> {
                when (sortOrder) {
                    SortOrder.ASCENDING -> m1.sellerLocation.country.compareTo(m2.sellerLocation.country)
                    SortOrder.DESCENDING -> m2.sellerLocation.country.compareTo(m1.sellerLocation.country)
                }
            }
            SortField.LANGUAGE -> {
                when (sortOrder) {
                    SortOrder.ASCENDING -> m1.productLanguage.displayName.compareTo(m2.productLanguage.displayName)
                    SortOrder.DESCENDING -> m2.productLanguage.displayName.compareTo(m1.productLanguage.displayName)
                }
            }
            SortField.SELLER_NAME -> {
                when (sortOrder) {
                    SortOrder.ASCENDING -> m1.sellerName.compareTo(m2.sellerName)
                    SortOrder.DESCENDING -> m2.sellerName.compareTo(m1.sellerName)
                }
            }
        }
    }
}

