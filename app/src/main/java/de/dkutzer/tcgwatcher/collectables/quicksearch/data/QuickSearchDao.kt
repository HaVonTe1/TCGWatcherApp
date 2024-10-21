package de.dkutzer.tcgwatcher.collectables.quicksearch.data

import androidx.room.Dao
import androidx.room.Query
import de.dkutzer.tcgwatcher.collectables.quicksearch.domain.PokemonCardQuickEntityWithMatchInfo


@Dao
interface QuickSearchDao {


    @Query(
        """
          SELECT qs_pokemon_cards.* , matchinfo(qs_fts_pokemon_cards_fts) as matchInfo
          FROM qs_pokemon_cards
          JOIN qs_fts_pokemon_cards_fts ON qs_pokemon_cards.id = qs_fts_pokemon_cards_fts.id
          WHERE qs_fts_pokemon_cards_fts MATCH :query
    """
    )
    fun fullTextSearchOverAllColumns(query: String): List<PokemonCardQuickEntityWithMatchInfo>



    @Query(
        """
          SELECT qs_pokemon_cards.* , matchinfo(qs_fts_pokemon_cards_fts) as matchInfo
          FROM qs_pokemon_cards
          JOIN qs_fts_pokemon_cards_fts ON qs_pokemon_cards.id = qs_fts_pokemon_cards_fts.id
          WHERE qs_fts_pokemon_cards_fts.names MATCH :query
          AND qs_pokemon_cards.code like :code
    """
    )
    fun fullTextSearchWithCode(query: String, code: String): List<PokemonCardQuickEntityWithMatchInfo>

}
