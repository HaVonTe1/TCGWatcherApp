package de.dkutzer.tcgwatcher.products.domain.port

import de.dkutzer.tcgwatcher.products.domain.SearchWithResultsEntity


interface SearchCacheRepository {

    suspend fun findBySearchTerm(searchTerm: String, page: Int , limit : Int = 5) : SearchWithResultsEntity?
    suspend fun persistsSearch(results: SearchWithResultsEntity)
}

class SearchCacheRepositoryImpl(private val searchCacheDao: SearchCacheDao) : SearchCacheRepository {

    override suspend fun findBySearchTerm(searchTerm: String,page: Int, limit: Int ): SearchWithResultsEntity? =
        searchCacheDao.findBySearchTerm(searchTerm, limit, (page-1)*limit)

    override suspend fun persistsSearch(results: SearchWithResultsEntity) {

        println("not yet impl")
    }

}