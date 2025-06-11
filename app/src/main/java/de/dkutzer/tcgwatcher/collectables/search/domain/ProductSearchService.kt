package de.dkutzer.tcgwatcher.collectables.search.domain

import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.settings.domain.BaseConfig

interface ProductSearchService {
    val client: ProductsApiClient
    val cache: SearchCacheRepository
    val config: BaseConfig

    suspend fun loadQuicksearchProductIntoResultPage(quickSearchProduct: ProductModel) : SearchResultsPage

    suspend fun refreshProduct(productModel: ProductModel, cacheOnly: Boolean) : ProductModel

    suspend fun searchByPage(searchString : String, page: Int = 1, limit: Int = 5) : SearchResultsPage

    suspend fun searchByOffset(searchString: String, limit: Int, offset: Int): SearchResultsPage {
        return searchByPage(searchString, page =  (limit+offset).div(limit), limit = limit)
    }
}