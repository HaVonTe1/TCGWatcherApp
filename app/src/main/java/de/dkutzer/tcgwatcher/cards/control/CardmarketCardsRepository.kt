package de.dkutzer.tcgwatcher.cards.control

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import de.dkutzer.tcgwatcher.cards.entity.SearchProductModel
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private val logger = KotlinLogging.logger {}

interface CardmarketPokemonRepository {
    fun getPokemonList(): Flow<PagingData<SearchProductModel>>
}

class GetPokemonList(
    private val pokemonRepository: CardmarketPokemonRepository
) {
    operator fun invoke(): Flow<PagingData<SearchProductModel>> {
        logger.debug { "Update Pokemonlist Flow" }
        return pokemonRepository.getPokemonList()
            .flowOn(Dispatchers.IO)
    }
}

class CardmarketPokemonRepositoryAdapter(
    private val pokemonPager: Pager<Int, SearchResultItemEntity>,
) : CardmarketPokemonRepository {
    override fun getPokemonList(): Flow<PagingData<SearchProductModel>> {
        return pokemonPager.flow.map { pagingDate ->
            pagingDate.map { it.toModel() }
        }
    }

}