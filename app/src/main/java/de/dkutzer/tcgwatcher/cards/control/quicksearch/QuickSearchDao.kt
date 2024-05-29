package de.dkutzer.tcgwatcher.cards.control.quicksearch

import androidx.room.Dao
import androidx.room.Query
import de.dkutzer.tcgwatcher.cards.entity.PokemonCardQuickEntityWithMatchInfo


@Dao
interface QuickSearchDao {


    @Query(
        """
          SELECT * , matchinfo(qs_pokemon_cards_fts) as matchInfo
          FROM qs_pokemon_cards
          JOIN qs_pokemon_cards_fts ON qs_pokemon_cards.external_id = qs_pokemon_cards_fts.external_id
          WHERE qs_pokemon_cards_fts MATCH :query
    """
    )
    fun search(query: String): List<PokemonCardQuickEntityWithMatchInfo>

}
