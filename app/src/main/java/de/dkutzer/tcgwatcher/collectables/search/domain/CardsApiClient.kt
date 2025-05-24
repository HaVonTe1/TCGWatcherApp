package de.dkutzer.tcgwatcher.collectables.search.domain

interface CardsApiClient {
    suspend fun search(searchString: String, page: Int = 1): SearchResultsPageDto
    suspend fun search(searchString: String, offset: Int , limit: Int): SearchResultsPageDto {
        return search(searchString, (offset + limit).floorDiv(limit))
    }
    suspend fun getProductDetails(link: String): CardmarketProductDetailsDto
}