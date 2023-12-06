package de.dkutzer.tcgwatcher.products.adapter.port

import de.dkutzer.tcgwatcher.products.adapter.api.ProductDetailsDto
import de.dkutzer.tcgwatcher.products.services.SearchResults

interface ProductRepository {

    fun getProductDetails(link: String) : ProductDetailsDto

    fun search(searchString : String, page: Int = 1) : SearchResults
}