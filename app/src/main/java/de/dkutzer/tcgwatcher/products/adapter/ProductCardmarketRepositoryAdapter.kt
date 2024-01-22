package de.dkutzer.tcgwatcher.products.adapter

import de.dkutzer.tcgwatcher.products.adapter.api.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.products.adapter.port.ProductRepository
import de.dkutzer.tcgwatcher.products.domain.*
import de.dkutzer.tcgwatcher.products.domain.port.SearchCacheRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.OffsetDateTime
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class ProductCardmarketRepositoryAdapter(
    private val client: BaseCardmarketApiClient,
    private val cache: SearchCacheRepository
) :
    ProductRepository {

    override suspend fun getProductDetails(link: String): ProductDetailsDto {

        return client.getProductDetails(link)
    }


    override suspend fun searchByPage(searchString: String, page: Int, limit: Int): SearchResults {

        logger.debug { "New Search in Adapter" }
        if(searchString.isEmpty())
            return SearchResults(emptyList(),1,1)
        lateinit var result: SearchResults
        logger.debug { "Looking in the Cache for: $searchString" }


        val searchWithResults = cache.findBySearchTerm(searchString, page)
        logger.trace { "Found: ${searchWithResults?.results?.size}" }
        if(searchWithResults!=null) {
            //TODO: handle lastUpdated for refreshing
            val searchItems = searchWithResults.results.map { it.toSearchItem() }
            result = SearchResults(searchItems, page, searchWithResults.search.size.floorDiv(limit).plus(1))
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
                    searchResults.totalPages)
                logger.trace { "Results so far: $mergedResults" }
                while(searchResults.page<searchResults.totalPages) {
                    logger.debug { "traversing pagination with total pages: ${searchResults.totalPages}" }
                    val newPage = searchResults.page +1
                    logger.trace { "Requesting the Api with $searchString for page: $newPage" }

                    searchResults = client.search(searchString, newPage)

                    mergedResults = SearchResultsPageDto(
                        mergedResults.results.plus(searchResults.results),
                        newPage,
                        searchResults.totalPages)
                    logger.trace { "Results so far: $mergedResults" }

                }


                val searchWithResultsEntity = SearchWithResultsEntity(
                    search = SearchEntity(
                        searchTerm = searchString,
                        size = mergedResults.results.size,
                        lastUpdated = OffsetDateTime.now().toEpochSecond()
                    ),
                    results = mergedResults.results.map { it.toSearchItemEntity() }.toList()
                )
                logger.debug { "Persisting cache: $searchWithResultsEntity" }
                cache.persistsSearch(searchWithResultsEntity)

                logger.debug { "Now fetching paged results from newly cache" }
                val updatedSearchResult = cache.findBySearchTerm(searchString, page)


                val searchItems = updatedSearchResult?.results?.map { it.toSearchItem() }
                result = SearchResults(searchItems?: listOf(), page, updatedSearchResult?.search?.size?.floorDiv(limit)?.plus(1)?:0)
            }
            logger.debug { "Duration: $duration" }


        }
        logger.debug { "Final result: $result" }
        return result

    }

}

