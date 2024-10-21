package de.dkutzer.tcgwatcher.collectables.search.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface CardmarketPokemonRepository {
    fun getPokemonList(): Flow<PagingData<ProductModel>>
}