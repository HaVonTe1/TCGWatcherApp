package de.dkutzer.tcgwatcher.cards.control.cache

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import de.dkutzer.tcgwatcher.cards.boundary.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.cards.entity.ProductItemEntity
import de.dkutzer.tcgwatcher.cards.entity.ProductModel
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}


abstract class PokemonPager {
    //Highlander Pattern
    companion object {
        @Volatile
        private var Instance: Pager<Int, ProductItemEntity>? = null

        @OptIn(ExperimentalPagingApi::class)
        fun providePokemonPager(
            searchTerm: String,
            refreshItem: ProductModel?,
            pokemonDatabase: SearchCacheDatabase,
            pokemonApi: BaseCardmarketApiClient,
        ): Pager<Int, ProductItemEntity> {

            logger.debug { "create Searching Pager" }
            return Instance ?: synchronized(this) {
                return Pager(
                    config = PagingConfig(pageSize = 5),
                    remoteMediator = SearchRemoteMediator(
                        searchTerm = searchTerm,
                        refreshItem,
                        pokemonDatabase = pokemonDatabase,
                        pokemonApi = pokemonApi,
                    ),
                    pagingSourceFactory = {
                        if(searchTerm.isEmpty() && refreshItem!=null) {
                            logger.debug { "calling refresh item as paging source" }
                            pokemonDatabase.searchCacheDao.findItemsByQuery(refreshItem.detailsUrl)
                        }
                        else {
                            logger.debug { "calling search term as paging source" }
                            pokemonDatabase.searchCacheDao.findItemsByQuery(searchTerm)
                        }
                    },
                )
            }
        }
    }

}

