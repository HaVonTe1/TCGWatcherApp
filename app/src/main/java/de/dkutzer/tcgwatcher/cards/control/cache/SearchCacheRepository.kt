package de.dkutzer.tcgwatcher.cards.control.cache

import de.dkutzer.tcgwatcher.cards.entity.SearchEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchWithResultsEntity


interface SearchCacheRepository {

    suspend fun findBySearchTerm(searchTerm: String, page: Int , limit : Int = 5) : SearchWithResultsEntity?
    suspend fun persistsSearch(results: SearchWithResultsEntity)
    suspend fun getSearchHistory(): List<String>
    suspend fun deleteSearch(search: SearchEntity)
    suspend fun deleteSearchResults(results: List<SearchResultItemEntity>)
}

class SearchCacheRepositoryImpl(private val searchCacheDao: SearchCacheDao) :
    SearchCacheRepository {

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

    override suspend fun deleteSearch(search: SearchEntity) {
        searchCacheDao.removeSearch(search)
    }

    override suspend fun deleteSearchResults(results: List<SearchResultItemEntity>) {
        searchCacheDao.removeResults(results)
    }
}