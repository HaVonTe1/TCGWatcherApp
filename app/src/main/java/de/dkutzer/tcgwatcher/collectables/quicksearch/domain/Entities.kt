package de.dkutzer.tcgwatcher.collectables.quicksearch.domain

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey


@Entity(tableName = "qs_pokemon_cards")
data class PokemonCardQuickNormalizedEntity (
    @PrimaryKey(autoGenerate = false)
    val id: String = "",

    @ColumnInfo(name = "name_de")
    val nameDe: String,
    @ColumnInfo(name = "name_en")
    val nameEn: String,
    @ColumnInfo(name = "name_fr")
    val nameFr: String,

    val code: String,

    @ColumnInfo(name = "cm_set_id")
    val cmSetId: String,
    @ColumnInfo(name = "cm_card_id")
    val cmCardId: String,
)



@Entity(tableName = "qs_fts_pokemon_cards_fts")
@Fts4(contentEntity = PokemonCardQuickEntity::class)
data class PokemonCardQuickEntityFTS(
    val names: String,
    val code: String,
    val id: String
)

@Entity(tableName = "qs_fts_pokemon_cards")
data class PokemonCardQuickEntity (
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    val names: String,
    val code: String,
)

data class PokemonCardQuickEntityWithMatchInfo(
    @Embedded
    val pokemonCardQuickEntity: PokemonCardQuickNormalizedEntity,
    @ColumnInfo(name = "matchInfo")
    val matchInfo: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PokemonCardQuickEntityWithMatchInfo

        if (pokemonCardQuickEntity != other.pokemonCardQuickEntity) return false
        if (!matchInfo.contentEquals(other.matchInfo)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pokemonCardQuickEntity.hashCode()
        result = 31 * result + matchInfo.contentHashCode()
        return result
    }
}