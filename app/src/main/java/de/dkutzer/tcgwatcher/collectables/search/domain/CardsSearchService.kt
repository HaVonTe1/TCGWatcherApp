package de.dkutzer.tcgwatcher.collectables.search.domain

interface CardsSearchService {

    suspend fun getSingleItemByItem(searchItem: ProductModel, useCache: Boolean = false) : SearchResultsPage

    suspend fun searchByPage(searchString : String, page: Int = 1, limit: Int = 5) : SearchResultsPage

    suspend fun searchByOffset(searchString: String, limit: Int, offset: Int): SearchResultsPage {
        return searchByPage(searchString, page =  (limit+offset).div(limit), limit = limit)
    }
}