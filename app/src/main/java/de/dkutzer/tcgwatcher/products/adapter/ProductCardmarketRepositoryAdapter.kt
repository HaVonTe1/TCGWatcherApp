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


    override suspend fun search(searchString: String, page: Int): SearchResults {

        lateinit var result: SearchResults
        logger.debug { "Looking in the Cache for: $searchString" }


        val searchWithResults = cache.findBySearchTerm(searchString)
        if(searchWithResults!=null) {
            //TODO: handle lastUpdated for refreshing
            val searchItems = searchWithResults.results.map { it.toSearchItem() }.toList()
            result = SearchResults(searchItems, page, 1) //FIXME
        }
        else {
            logger.debug { "Start a new Search: $searchString" }
            val duration = measureTimeMillis {
                val searchResults = client.search(searchString, page)
                val searchWithResultsEntity = SearchWithResultsEntity(
                    search = SearchEntity(
                        searchTerm = searchString,
                        lastUpdated = OffsetDateTime.now()
                    ),
                    results = searchResults.results.map { it.toSearchItemEntity() }.toList()
                )
                cache.persistsSearch(searchWithResultsEntity)

                val searchItems = searchResults.results.map { it.toSearchItem() }.toList()
                result = SearchResults(searchItems, page, searchResults.totalPages)
            }
            logger.debug { "Duration: $duration" }


        }
        return result

    }
    private fun SearchResultItemDto.toSearchItem( ) : SearchItem {
        return SearchItem(
            this.displayName,
            this.orgName,
            this.cmLink,
            this.imgLink,
            this.price)
    }

    private fun SearchResultItemDto.toSearchItemEntity() : SearchResultItemEntity {
        return SearchResultItemEntity(
            displayName = this.displayName,
            imgLink = this.imgLink,
            orgName = this.orgName,
            price = this.price,
            cmLink = this.cmLink,
            searchId = 0 //this wont work, will it?
        )
    }

    private fun SearchResultItemEntity.toSearchItem() : SearchItem {
        return SearchItem(
            displayName = this.displayName,
            orgName = this.orgName,
            cmLink = this.cmLink,
            imgLink = this.imgLink,
            price = this.price

        )
    }


}

