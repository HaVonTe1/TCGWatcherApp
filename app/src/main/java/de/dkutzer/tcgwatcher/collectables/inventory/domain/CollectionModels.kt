package de.dkutzer.tcgwatcher.collectables.inventory.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * Repräsentiert eine Sammlung/Inventar (z.B. "Meine Karten", "Wunschliste", ...)
 */
@Parcelize
data class CollectionModel(
    val id: Long = 0,
    val name: String,
    val description: String? = null
) : Parcelable

/**
 * Ein Eintrag in einer Sammlung, der eine Karte/Produkt mit Attributen enthält
 */
@Parcelize
data class CollectionEntryModel(
    val id: Long = 0,
    val collectionId: Long,
    val productId: String, // Referenz auf ProductModel.id
    val condition: CardCondition,
    val language: CardLanguage,
    val price: Double?,
    val origin: CardOrigin,
    val date: LocalDate
) : Parcelable

enum class CardCondition { NM, EX, GD, PL, POOR }
enum class CardLanguage { DE, EN, JP, FR, IT, ES, OTHER }
enum class CardOrigin { BOOSTER, ONLINE_ORDER, TRADED, GIFT, OTHER }
