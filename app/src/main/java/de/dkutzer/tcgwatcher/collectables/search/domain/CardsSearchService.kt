package de.dkutzer.tcgwatcher.collectables.search.domain

import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.settings.domain.BaseConfig

interface CardsSearchService {
    val client: CardsApiClient
    val cache: SearchCacheRepository
    val config: BaseConfig

    suspend fun getSingleItemByItem(searchItem: ProductModel, useCache: Boolean, useTtl: Boolean,loadDetails: Boolean) : SearchResultsPage

    suspend fun getProductWithDetails(productId: String, useCache: Boolean) : ProductModel

    suspend fun searchByPage(searchString : String, page: Int = 1, limit: Int = 5) : SearchResultsPage

    suspend fun searchByOffset(searchString: String, limit: Int, offset: Int): SearchResultsPage {
        return searchByPage(searchString, page =  (limit+offset).div(limit), limit = limit)
    }
}