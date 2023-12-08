package de.dkutzer.tcgwatcher.products.adapter.port

import de.dkutzer.tcgwatcher.products.adapter.api.ProductDetailsDto
import de.dkutzer.tcgwatcher.products.services.SearchResults

interface ProductRepository {

    suspend fun getProductDetails(link: String) : ProductDetailsDto

    suspend fun search(searchString : String, page: Int = 1) : SearchResults
}