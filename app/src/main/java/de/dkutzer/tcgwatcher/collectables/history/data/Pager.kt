package de.dkutzer.tcgwatcher.collectables.history.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductSearchService
import de.dkutzer.tcgwatcher.collectables.search.domain.RefreshState
import de.dkutzer.tcgwatcher.collectables.search.domain.RefreshWrapper
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
        private var Instance: Pager<Int, ProductWithSellOffers>? = null

        @OptIn(ExperimentalPagingApi::class)
        fun providePokemonPager(
            searchTerm: String,
            refreshModel: RefreshWrapper,
            quicksearchItem: ProductModel? = null,
            pokemonDatabase: SearchCacheDatabase,
            cardSearchService: ProductSearchService
        ): Pager<Int, ProductWithSellOffers> {

            logger.debug { "create Searching Pager" }
            return Instance ?: synchronized(this) {
                logger.debug { "Pager: instance" }
                return Pager(
                    config = PagingConfig(pageSize = 5),
                    remoteMediator = SearchRemoteMediator(

                        searchTerm = searchTerm,
                        refreshModel = refreshModel,
                        quicksearchItem = quicksearchItem,
                        searchCacheDatabase = pokemonDatabase,
                        productSearchService = cardSearchService
                    ),
                    pagingSourceFactory = {
                        logger.debug { "refreshItem: [$refreshModel] searchTerm: [$searchTerm]" }
                        //TODO: this can be removed??!!!??!?!?
                        if (refreshModel.state == RefreshState.REFRESH_ITEM || refreshModel.state == RefreshState.REFRESH_ITEM_FROM_CACHE) {
                            logger.debug { "calling refresh item as paging source (state: ${refreshModel.state}): $refreshModel" }

                            pokemonDatabase.searchCacheDao.getProductWithSellOffersPagingSource(refreshModel.query)
                        }
                        else if(quicksearchItem != null) {
                            logger.debug { "calling quicksearch  as paging source: $quicksearchItem" }
                            pokemonDatabase.searchCacheDao.getProductWithSellOffersPagingSource(quicksearchItem.detailsUrl)
                        }
                        else {
                            logger.debug { "calling search term as paging source: $searchTerm" }
                            pokemonDatabase.searchCacheDao.getProductWithSellOffersPagingSource(searchTerm)
                        }
                    },
                )
            }
        }
    }

}

