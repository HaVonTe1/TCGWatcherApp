package de.dkutzer.tcgwatcher.products.adapter.port

import de.dkutzer.tcgwatcher.products.domain.ProductDetailsDto
import de.dkutzer.tcgwatcher.products.domain.SearchResults

interface ProductRepository {

    suspend fun getProductDetails(link: String) : ProductDetailsDto

    suspend fun search(searchString : String, page: Int = 1, limit: Int = 5) : SearchResults
}