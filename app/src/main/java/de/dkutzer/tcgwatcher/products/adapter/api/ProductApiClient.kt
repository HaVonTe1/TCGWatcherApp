package de.dkutzer.tcgwatcher.products.adapter.api

import de.dkutzer.tcgwatcher.products.domain.ProductDetailsDto
import de.dkutzer.tcgwatcher.products.domain.SearchResultsPageDto

interface ProductApiClient {
    suspend fun search(searchString: String, page: Int = 1): SearchResultsPageDto
    suspend fun search(searchString: String, offset: Int , limit: Int): SearchResultsPageDto {
        return search(searchString, (offset + limit).floorDiv(limit))
    }

    suspend fun getProductDetails(link: String): ProductDetailsDto
}


