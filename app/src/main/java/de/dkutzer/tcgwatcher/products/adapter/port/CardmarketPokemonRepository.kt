package de.dkutzer.tcgwatcher.products.adapter.port

import androidx.paging.PagingData
import de.dkutzer.tcgwatcher.products.domain.SearchProductModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

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