package de.dkutzer.tcgwatcher.products.adapter

import de.dkutzer.tcgwatcher.products.adapter.api.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.products.adapter.api.ProductDetailsDto
import de.dkutzer.tcgwatcher.products.adapter.api.SearchResultItemDto
import de.dkutzer.tcgwatcher.products.adapter.port.ProductRepository
import de.dkutzer.tcgwatcher.products.services.SearchItem
import de.dkutzer.tcgwatcher.products.services.SearchResults
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class ProductCardmarketRepositoryAdapter(private val client: BaseCardmarketApiClient) :
    ProductRepository {

    override suspend fun getProductDetails(link: String): ProductDetailsDto {

        return client.getProductDetails(link)
    }


    override suspend fun search(searchString: String, page: Int): SearchResults {

        logger.debug { "Start a new Search: $searchString" }
        lateinit var result: SearchResults
        val duration = measureTimeMillis {
            val searchResults = client.search(searchString, page)

            val searchItems = searchResults.results.map { it.toSearchItem() }.toList()
            result = SearchResults(searchItems, page, searchResults.totalPages)
        }
        logger.debug { "Duration: $duration" }
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


}

