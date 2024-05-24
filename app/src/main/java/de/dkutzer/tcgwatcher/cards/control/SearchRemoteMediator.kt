package de.dkutzer.tcgwatcher.cards.control

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import de.dkutzer.tcgwatcher.cards.boundary.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.cards.entity.RemoteKeyEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemEntity
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}


@OptIn(ExperimentalPagingApi::class)
class SearchRemoteMediator (
    private val searchTerm: String,
    private val pokemonDatabase: SearchCacheDatabase,
    pokemonApi: BaseCardmarketApiClient,
) : RemoteMediator<Int, SearchResultItemEntity>() {

    private val REMOTE_KEY_ID = "cm"
    private val searchCacheRepository = SearchCacheRepositoryImpl(pokemonDatabase.searchCacheDaoDa)
    private val adapter = CardmarketCardsRepositoryAdapter(pokemonApi,searchCacheRepository )

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, SearchResultItemEntity>,
    ): MediatorResult {

        logger.debug { "Mediator load: $loadType" }
        val offset = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                // RETRIEVE NEXT OFFSET FROM DATABASE
                val remoteKey = pokemonDatabase.remoteKeyDao.getById(REMOTE_KEY_ID)
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
        val searchByOffset =
            adapter.searchByOffset(searchTerm, limit = state.config.pageSize, offset = offset)
        logger.debug { "SearchResult from Adapter: $searchByOffset" }
        // MAKE API CALL

        val nextOffset = offset + state.config.pageSize
        logger.debug { "Next Offset: $nextOffset" }
        // SAVE RESULTS AND NEXT OFFSET TO DATABASE
        pokemonDatabase.withTransaction {

            logger.debug { "Upsert remotekey" }

            pokemonDatabase.remoteKeyDao.insert(
                RemoteKeyEntity(
                    id = REMOTE_KEY_ID,
                    nextOffset = nextOffset,
                )
            )
        }
        // CHECK IF END OF PAGINATION REACHED
        // i dunno if this always works. what if the total number of resuls is equal to the pageSize?
        return MediatorResult.Success(endOfPaginationReached = searchByOffset.items.size < state.config.pageSize)
    }

}