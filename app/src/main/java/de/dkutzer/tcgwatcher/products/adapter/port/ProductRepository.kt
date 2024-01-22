package de.dkutzer.tcgwatcher.products.adapter.port

import de.dkutzer.tcgwatcher.products.domain.ProductDetailsDto
import de.dkutzer.tcgwatcher.products.domain.SearchResults

interface ProductRepository {

    suspend fun getProductDetails(link: String) : ProductDetailsDto

    suspend fun searchByPage(searchString : String, page: Int = 1, limit: Int = 5) : SearchResults

    suspend fun searchByOffset(searchString: String, limit: Int, offset: Int): SearchResults {
        return searchByPage(searchString, page =  (limit+offset).div(limit), limit = limit)
    }
}