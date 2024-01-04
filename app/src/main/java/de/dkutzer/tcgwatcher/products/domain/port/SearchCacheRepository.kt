package de.dkutzer.tcgwatcher.products.domain.port

import de.dkutzer.tcgwatcher.products.domain.SearchWithResultsEntity


interface SearchCacheRepository {

    suspend fun findBySearchTerm(searchTerm: String) : SearchWithResultsEntity?
    suspend fun persistsSearch(results: SearchWithResultsEntity)
}

class SearchCacheRepositoryImpl(private val searchCacheDao: SearchCacheDao) : SearchCacheRepository {

    override suspend fun findBySearchTerm(searchTerm: String): SearchWithResultsEntity? =
        searchCacheDao.findBySearchTerm(searchTerm)

    override suspend fun persistsSearch(results: SearchWithResultsEntity) = searchCacheDao.persistResults(results)

}