package de.dkutzer.tcgwatcher.products.adapter.port

import de.dkutzer.tcgwatcher.products.domain.port.ProductDetails
import de.dkutzer.tcgwatcher.products.domain.port.SearchItem

interface ProductRepository {

    fun getProductDetailsById(id: String) : ProductDetails

    fun search(searchString : String) : List<SearchItem>
}