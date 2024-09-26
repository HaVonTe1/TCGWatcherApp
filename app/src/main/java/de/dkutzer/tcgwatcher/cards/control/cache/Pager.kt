package de.dkutzer.tcgwatcher.cards.control.cache

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import de.dkutzer.tcgwatcher.cards.boundary.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.cards.entity.ProductItemEntity
import de.dkutzer.tcgwatcher.cards.entity.ProductModel
import de.dkutzer.tcgwatcher.cards.entity.RefreshState
import de.dkutzer.tcgwatcher.cards.entity.RefreshWrapper
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
            refreshModel: RefreshWrapper,
            quicksearchItem: ProductModel? = null,
            pokemonDatabase: SearchCacheDatabase,
            pokemonApi: BaseCardmarketApiClient,
        ): Pager<Int, ProductItemEntity> {

            logger.debug { "create Searching Pager" }
            return Instance ?: synchronized(this) {
                logger.debug { "Pager: instance" }
                return Pager(
                    config = PagingConfig(pageSize = 5),
                    remoteMediator = SearchRemoteMediator(
                        searchTerm = searchTerm,
                        refreshModel = refreshModel,
                        quicksearchItem = quicksearchItem,
                        pokemonDatabase = pokemonDatabase,
                        pokemonApi = pokemonApi,
                    ),
                    pagingSourceFactory = {
                        logger.debug { "refreshItem: [$refreshModel] searchTerm: [$searchTerm]" }
                        if(refreshModel.state == RefreshState.REFRESH_ITEM) {
                            logger.debug { "calling refresh item as paging source: $refreshModel" }
                            pokemonDatabase.searchCacheDao.findItemsByQuery(refreshModel.item!!.detailsUrl)
                        }
                        else if(quicksearchItem != null) {
                            logger.debug { "calling quicksearch  as paging source: $quicksearchItem" }
                            pokemonDatabase.searchCacheDao.findItemsByQuery(quicksearchItem.detailsUrl)
                        }
                        else {
                            logger.debug { "calling search term as paging source: $searchTerm" }
                            pokemonDatabase.searchCacheDao.findItemsByQuery(searchTerm)
                        }
                    },
                )
            }
        }
    }

}

