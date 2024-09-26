package de.dkutzer.tcgwatcher.cards.control

import de.dkutzer.tcgwatcher.cards.boundary.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.cards.control.cache.SearchCacheRepository
import de.dkutzer.tcgwatcher.cards.entity.ProductModel
import de.dkutzer.tcgwatcher.cards.entity.SearchEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchResultsPage
import de.dkutzer.tcgwatcher.cards.entity.SearchResultsPageDto
import de.dkutzer.tcgwatcher.cards.entity.SearchWithItemsEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import kotlin.system.measureTimeMillis

interface CardsSearchService {

    suspend fun getSingleItemByItem(searchItem: ProductModel, useCache: Boolean = false) : SearchResultsPage

    suspend fun searchByPage(searchString : String, page: Int = 1, limit: Int = 5) : SearchResultsPage

    suspend fun searchByOffset(searchString: String, limit: Int, offset: Int): SearchResultsPage {
        return searchByPage(searchString, page =  (limit+offset).div(limit), limit = limit)
    }
}

private val logger = KotlinLogging.logger {}


class CardmarketCardsSearchServiceAdapter(
    private val client: BaseCardmarketApiClient,
    private val cache: SearchCacheRepository
) : CardsSearchService {

    private val threeDaysSeconds = 3 * 24 * 60 * 60L //TODO: make it configurable


    override suspend fun getSingleItemByItem(searchItem: ProductModel, useCache: Boolean): SearchResultsPage {

        /*
        This use case is different from the search by query.
        We might dont have a "searchEntity" here. Because we came here via a refresh from a singleItemView.
        Only in the case, that we already refreshed this item, where is a "SearchEntity".
        In every case the SearchEntity with the cmLink as Id needs to pe upserted.
         */
        logger.debug { "Adapter: getSingleItemByLink: $searchItem" }
        logger.debug { "Adapter: Start a new load: ${searchItem.detailsUrl}" }

        val searchWithItemsEntity = if(useCache) {
            logger.debug { "Adapter: Looking in the Cache for: ${searchItem.detailsUrl}" }
            val cachedSearch = cache.findSearchWithItemsByQuery(searchItem.detailsUrl)
            logger.debug { "Adapter: Found: $cachedSearch" }
            if(cachedSearch==null || cachedSearch.isOlderThan(threeDaysSeconds)){
                logger.debug { "Adapter: Cache is older than 3 days: ${Instant.ofEpochMilli(cachedSearch?.search?.lastUpdated!!)}" }
                val newSearch =
                    createNewSearchModelViaRemoteRequestByProductModel(searchItem)

                logger.debug { "Adapter: Persisting cache: $newSearch" }

                cache.persistsSearchWithItems(newSearch)
            } else {
                logger.debug { "Adapter: Returning cached results: $cachedSearch" }
                cachedSearch
            }
        }
        else {
            logger.debug { "Adapter: no cache - Start a new Search: ${searchItem.detailsUrl}" }
            val newSearch = createNewSearchModelViaRemoteRequestByProductModel(searchItem)
            logger.debug { "Adapter: Persisting cache: $newSearch" }

            cache.persistsSearchWithItems(newSearch)
        }

        val productModel = searchWithItemsEntity.results.first().toProductModel()
        val result = SearchResultsPage(listOf(productModel), 1, 1)

        //make sure the refreshed data is mirrored to all search items with this link
        //TODO: another nested table which stores the items and the searchitems_table just references to it
        cache.updateItemByLink(searchItem.detailsUrl, productModel.toSearchResultItemEntity())

        logger.debug { "Adapter: Returning cached results: $result" }
        return result
    }

    private suspend fun createNewSearchModelViaRemoteRequestByProductModel(searchItem: ProductModel) : SearchWithItemsEntity{
        val productDetails = client.getProductDetails(searchItem.detailsUrl)
        val productModel = productDetails.toProductModel()
        val searchWithItemsEntity = SearchWithItemsEntity(
            search = SearchEntity(
                searchTerm = searchItem.detailsUrl,
                size = 1,
                lastUpdated = Instant.now().epochSecond,
                history = false
            ),
            results = listOf(productModel.toSearchResultItemEntity())
        )
        return searchWithItemsEntity
    }

    override suspend fun searchByPage(searchString: String, page: Int, limit: Int): SearchResultsPage {
        logger.debug { "Adapter: New Search by searchTerm: $searchString}" }
        if(searchString.isEmpty())
            return SearchResultsPage(emptyList(), 1, 1)
        lateinit var result: SearchResultsPage
        logger.debug { "Adapter: Looking in the Cache for: $searchString" }

        var searchWithResults = cache.findSearchWithItemsByQuery(searchString, page)
        logger.debug { "Adapter: Found: ${searchWithResults?.results?.size}" }
        val lastUpdated = searchWithResults?.search?.lastUpdated
        if(lastUpdated!=null)
            logger.debug { "TTL: ${Instant.ofEpochSecond(lastUpdated)}" }

        if(searchWithResults!=null && searchWithResults.isOlderThan(threeDaysSeconds)) {
            logger.debug { "Adapter: Cache is older than 3 days: ${Instant.ofEpochMilli(lastUpdated!!)}" }

            cache.deleteSearchItems(searchWithResults.results)
            cache.deleteSearch(searchWithResults.search)
            searchWithResults = null
        }
        if(searchWithResults!=null && !searchWithResults.isOlderThan(threeDaysSeconds)) {
            val searchItems = searchWithResults.results.map { it.toProductModel() }
            result = SearchResultsPage(
                searchItems,
                page,
                searchWithResults.search.size.floorDiv(limit).plus(1)
            )
            logger.trace { "Adapter: Returning cached results: $result" }
        }
        else {
            logger.debug { "Adapter: Start a new Search: $searchString" }
            val duration = measureTimeMillis {

                logger.trace { "Adapter: Requesting the Api with $searchString for page: 1" }
                var searchResults = client.search(searchString, 1)
                var mergedResults = SearchResultsPageDto(
                    searchResults.results,
                    1,
                    searchResults.totalPages
                )
                logger.trace { "Adapter: Results so far: $mergedResults" }
                while (searchResults.page < searchResults.totalPages) {
                    logger.debug { "Adapter: traversing pagination with total pages: ${searchResults.totalPages}" }
                    val newPage = searchResults.page + 1
                    logger.trace { "Adapter: Requesting the Api with $searchString for page: $newPage" }

                    searchResults = client.search(searchString, newPage)

                    mergedResults = SearchResultsPageDto(
                        mergedResults.results.plus(searchResults.results),
                        newPage,
                        searchResults.totalPages
                    )
                    logger.trace { "Adapter: Results so far: $mergedResults" }
                }

                val searchWithItemsEntity = SearchWithItemsEntity(
                    search = SearchEntity(
                        searchTerm = searchString,
                        size = mergedResults.results.size,
                        lastUpdated = Instant.now().epochSecond,
                        history = true
                    ),
                    results = mergedResults.results.map { it.toSearchItemEntity() }.toList()
                )
                logger.debug { "Persisting cache: $searchWithItemsEntity" }
                cache.persistsSearchWithItems(searchWithItemsEntity)

                logger.debug { "Now fetching paged results from newly cache" }
                val updatedSearchResult = cache.findSearchWithItemsByQuery(searchString, page)


                val searchItems = updatedSearchResult?.results?.map { it.toProductModel() }
                result = SearchResultsPage(
                    searchItems ?: listOf(),
                    page,
                    updatedSearchResult?.search?.size?.floorDiv(limit)?.plus(1) ?: 0
                )
            }
            logger.debug { "Duration: $duration" }
        }
        logger.debug { "Final result: $result" }
        return result
    }
}