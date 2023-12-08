package de.dkutzer.tcgwatcher.products.adapter.api

interface ProductApiClient {
    suspend fun search(searchString: String, page: Int = 1): SearchResultsPageDto

    suspend fun getProductDetails(link: String): ProductDetailsDto
}

data class SearchResultsPageDto(
    val results: List<SearchResultItemDto>,
    val page: Int,
    val totalPages: Int
)

data class SearchResultItemDto(
    val displayName: String,
    val orgName: String,
    val cmLink: String,
    val imgLink: String,
    val price: String
)

data class ProductDetailsDto(
    val imageUrl: String,
    val localPrice: String,
    val localPriceTrend: String
)
