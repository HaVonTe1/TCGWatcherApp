package de.dkutzer.tcgwatcher.products.adapter.api

interface ProductApiClient {
    fun search(searchString: String): SearchItemsDto

    fun getProductDetails(link: String): ProductDetailsDto
}

data class SearchItemsDto(val htmlCode: String)

data class ProductDetailsDto(val htmlCode: String)
