package de.dkutzer.tcgwatcher.collectables.history.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
import de.dkutzer.tcgwatcher.collectables.search.data.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.RefreshState
import de.dkutzer.tcgwatcher.collectables.search.domain.RefreshWrapper
import de.dkutzer.tcgwatcher.settings.domain.BaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}


/**
 * `PokemonPager` is an abstract class responsible for providing a `Pager` instance
 * for fetching and managing a paginated list of `ProductItemEntity` objects,
 * representing Pokemon card data.
 *
 * This class utilizes the Highlander pattern to ensure that only a single `Pager`
 * instance is created and shared throughout the application. It handles different
 * types of data retrieval, such as searching by a term, refreshing a specific item,
 * or performing a quick search.
 */
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
            config: BaseConfig
        ): Pager<Int, ProductItemEntity> {

            logger.debug { "create Searching Pager" }
            return Instance ?: synchronized(this) {
                logger.debug { "Pager: instance" }
                return Pager(
                    config = PagingConfig(pageSize = 5),
                    remoteMediator = SearchRemoteMediator(
                        config = config,
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

