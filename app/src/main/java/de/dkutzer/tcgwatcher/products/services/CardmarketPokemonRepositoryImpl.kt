package de.dkutzer.tcgwatcher.products.services

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import de.dkutzer.tcgwatcher.products.adapter.port.CardmarketPokemonRepository
import de.dkutzer.tcgwatcher.products.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CardmarketPokemonRepositoryImpl(
    private val pokemonPager: Pager<Int, SearchResultItemEntity>,
//    private val productRepository: ProductRepository,
//    private val productMapper: ProductMapper
) : CardmarketPokemonRepository {
    override fun getPokemonList(): Flow<PagingData<SearchProductModel>> {
        return pokemonPager.flow.map { pagingDate ->
            pagingDate.map { it.toModel() }
        }
    }
//
//    suspend fun search(searchString: String, page: Int) : SearchProductViewModel {
//        val searchResults = productRepository.search(searchString, page)
//        val productModels = searchResults.items.map {
//            productMapper.toModel(it)
//        }.toList()
//        return SearchProductViewModel(productModels, searchResults.pages)
//    }
}

