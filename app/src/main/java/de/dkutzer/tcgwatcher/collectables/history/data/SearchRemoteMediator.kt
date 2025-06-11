package de.dkutzer.tcgwatcher.collectables.history.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.RemoteKeyEntity
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductSearchService
import de.dkutzer.tcgwatcher.collectables.search.domain.RefreshWrapper
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}


@OptIn(ExperimentalPagingApi::class)
class SearchRemoteMediator (
    private val searchTerm: String,
    private val refreshModel: RefreshWrapper,
    private val quicksearchItem: ProductModel? = null,
    private val searchCacheDatabase: SearchCacheDatabase,
    private val productSearchService: ProductSearchService
) : RemoteMediator<Int, ProductWithSellOffers>() {

    private val REMOTE_KEY_ID = "cm"

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ProductWithSellOffers>,
    ): MediatorResult {

        logger.debug { "Mediator searchTerm: $searchTerm" }
        logger.debug { "Mediator refreshItem: $refreshModel" }
        logger.debug { "Mediator quicksearchItem: $quicksearchItem" }

        logger.debug { "Mediator load: $loadType" }
        logger.debug { "Mediator state: $state" }
        logger.debug { "Mediator state.config.pageSize: ${state.config.pageSize}" }
        logger.debug { "Mediator state.config.initialLoadSize: ${state.config.initialLoadSize}" }
        val offset = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                // RETRIEVE NEXT OFFSET FROM DATABASE
                val remoteKey = searchCacheDatabase.remoteKeyDao.getById(REMOTE_KEY_ID)
                if (remoteKey == null || remoteKey.nextOffset == 0) // END OF PAGINATION REACHED
                    return MediatorResult.Success(endOfPaginationReached = true)
                remoteKey.nextOffset
            }
        }

        logger.debug { "Offset: $offset" }
        // This would be the point where the mediator would make the api call
        // but we want to make at least calls as possible
        // so we make a call to fetch ALL data from the api and return the
        // cached room stuff

        val searchResultsPage =  if (quicksearchItem != null) {
            productSearchService.loadQuicksearchProductIntoResultPage(quicksearchItem)
        }
        else {
            productSearchService.searchByOffset(searchTerm, limit = state.config.pageSize, offset = offset)
        }
        logger.debug { "SearchResult from Adapter: $searchResultsPage" }
        logger.info {"SearchResult size: ${searchResultsPage.items.size}"}
        // MAKE API CALL

        val nextOffset = offset + state.config.pageSize
        logger.debug { "Next Offset: $nextOffset" }
        // SAVE RESULTS AND NEXT OFFSET TO DATABASE
        searchCacheDatabase.withTransaction {

            logger.debug { "Upsert remotekey" }

            searchCacheDatabase.remoteKeyDao.insert(
                RemoteKeyEntity(
                    id = REMOTE_KEY_ID,
                    nextOffset = nextOffset,
                )
            )
        }
        // CHECK IF END OF PAGINATION REACHED
        // i dunno if this always works. what if the total number of resuls is equal to the pageSize?
        return MediatorResult.Success(endOfPaginationReached = searchResultsPage.items.size < state.config.pageSize)
    }

}