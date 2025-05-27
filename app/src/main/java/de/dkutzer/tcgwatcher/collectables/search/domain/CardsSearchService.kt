package de.dkutzer.tcgwatcher.collectables.search.domain

interface CardsSearchService {

    suspend fun getSingleItemByItem(searchItem: ProductModel, useCache: Boolean, useTtl: Boolean,loadDetails: Boolean, language: String) : SearchResultsPage

    suspend fun searchByPage(searchString : String, page: Int = 1, limit: Int = 5, language: String) : SearchResultsPage

    suspend fun searchByOffset(searchString: String, limit: Int, offset: Int, language: String): SearchResultsPage {
        return searchByPage(searchString, page =  (limit+offset).div(limit), limit = limit, language = language)
    }
}