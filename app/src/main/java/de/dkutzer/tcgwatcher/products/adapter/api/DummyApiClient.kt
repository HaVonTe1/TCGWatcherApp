package de.dkutzer.tcgwatcher.products.adapter.api

import de.dkutzer.tcgwatcher.products.domain.ProductDetailsDto
import de.dkutzer.tcgwatcher.products.domain.SearchResultsPageDto

class DummyApiClient : BaseCardmarketApiClient() {
    override suspend fun search(searchString: String, page: Int): SearchResultsPageDto {
        TODO("Not yet implemented")
    }

    override suspend fun getProductDetails(link: String): ProductDetailsDto {
        TODO("Not yet implemented")
    }
}