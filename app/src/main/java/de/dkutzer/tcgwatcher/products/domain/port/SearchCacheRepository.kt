package de.dkutzer.tcgwatcher.products.domain.port

import de.dkutzer.tcgwatcher.products.domain.SearchEntity
import de.dkutzer.tcgwatcher.products.domain.SearchWithResultsEntity


interface SearchCacheRepository {

    suspend fun findBySearchTerm(searchTerm: String, page: Int , limit : Int = 5) : SearchWithResultsEntity?
    suspend fun persistsSearch(results: SearchWithResultsEntity)
    suspend fun getSearchHistory(): List<String>
}

class SearchCacheRepositoryImpl(private val searchCacheDao: SearchCacheDao) : SearchCacheRepository {

    override suspend fun findBySearchTerm(searchTerm: String,page: Int, limit: Int ): SearchWithResultsEntity?  {

        val search = searchCacheDao.findSearch(searchTerm)
        if(search!=null) {
            val resultItemEntities = searchCacheDao.findSearchResultsBySearchId(
                search.searchId,
                limit,
                (page - 1) * limit
            )
            return SearchWithResultsEntity(search = search, results =  resultItemEntities)
        }
        return null

    }

    override suspend fun persistsSearch(results: SearchWithResultsEntity) {

        val searchId = searchCacheDao.persistSearch(results.search)
        results.results.forEach { it.searchId = searchId.toInt() }

        searchCacheDao.persistResults(results.results)
    }

    override suspend fun getSearchHistory(): List<String> {
        return searchCacheDao.getSearchHistory()
    }
}