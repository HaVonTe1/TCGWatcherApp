package de.dkutzer.tcgwatcher.collectables.quicksearch.domain

interface QuickSearchRepository {

    suspend fun find(query: String): List<PokemonCardQuickNormalizedEntity>

}