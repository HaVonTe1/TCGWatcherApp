package de.dkutzer.tcgwatcher.cards.control

import de.dkutzer.tcgwatcher.cards.boundary.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.cards.control.cache.SearchCacheRepository
import de.dkutzer.tcgwatcher.cards.entity.CardDetailsDto
import de.dkutzer.tcgwatcher.cards.entity.SearchEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchResultsPage
import de.dkutzer.tcgwatcher.cards.entity.SearchResultsPageDto
import de.dkutzer.tcgwatcher.cards.entity.SearchWithResultsEntity
import de.dkutzer.tcgwatcher.cards.entity.isOlderThan
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import kotlin.system.measureTimeMillis

interface CardsSearchService {

    suspend fun getDetails(link: String) : CardDetailsDto

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

    override suspend fun getDetails(link: String): CardDetailsDto {
        return client.getProductDetails(link)
    }

    override suspend fun searchByPage(searchString: String, page: Int, limit: Int): SearchResultsPage {
        logger.debug { "New Search in Adapter" }
        if(searchString.isEmpty())
            return SearchResultsPage(emptyList(), 1, 1)
        lateinit var result: SearchResultsPage
        logger.debug { "Looking in the Cache for: $searchString" }

        var searchWithResults = cache.findBySearchTerm(searchString, page)
        logger.trace { "Found: ${searchWithResults?.results?.size}" }
        val threeDaysSeconds = 3 * 24 * 60 * 60L //TODO: make it configurable
        if(searchWithResults!=null && searchWithResults.isOlderThan(threeDaysSeconds)) {
            cache.deleteSearchResults(searchWithResults.results)
            cache.deleteSearch(searchWithResults.search)
            searchWithResults = null
        }
        if(searchWithResults!=null && !searchWithResults.isOlderThan(threeDaysSeconds)) {
            val searchItems = searchWithResults.results.map { it.toSearchItem() }
            result = SearchResultsPage(
                searchItems,
                page,
                searchWithResults.search.size.floorDiv(limit).plus(1)
            )
            logger.trace { "Returning cached results: $result" }
        }
        else {
            logger.debug { "Start a new Search: $searchString" }
            val duration = measureTimeMillis {

                logger.trace { "Requesting the Api with $searchString for page: 1" }
                var searchResults = client.search(searchString, 1)
                var mergedResults = SearchResultsPageDto(
                    searchResults.results,
                    1,
                    searchResults.totalPages
                )
                logger.trace { "Results so far: $mergedResults" }
                while (searchResults.page < searchResults.totalPages) {
                    logger.debug { "traversing pagination with total pages: ${searchResults.totalPages}" }
                    val newPage = searchResults.page + 1
                    logger.trace { "Requesting the Api with $searchString for page: $newPage" }

                    searchResults = client.search(searchString, newPage)

                    mergedResults = SearchResultsPageDto(
                        mergedResults.results.plus(searchResults.results),
                        newPage,
                        searchResults.totalPages
                    )
                    logger.trace { "Results so far: $mergedResults" }
                }

                val searchWithResultsEntity = SearchWithResultsEntity(
                    search = SearchEntity(
                        searchTerm = searchString,
                        size = mergedResults.results.size,
                        lastUpdated = Instant.now().epochSecond
                    ),
                    results = mergedResults.results.map { it.toSearchItemEntity() }.toList()
                )
                logger.debug { "Persisting cache: $searchWithResultsEntity" }
                cache.persistsSearch(searchWithResultsEntity)

                logger.debug { "Now fetching paged results from newly cache" }
                val updatedSearchResult = cache.findBySearchTerm(searchString, page)


                val searchItems = updatedSearchResult?.results?.map { it.toSearchItem() }
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