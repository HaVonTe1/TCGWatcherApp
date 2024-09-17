package de.dkutzer.tcgwatcher.cards.control.cache

import de.dkutzer.tcgwatcher.cards.entity.ProductItemEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchWithItemsEntity
import io.github.oshai.kotlinlogging.KotlinLogging


interface SearchCacheRepository {

    suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int, limit : Int = 5) : SearchWithItemsEntity?
    suspend fun persistsSearchWithItems(results: SearchWithItemsEntity)
    suspend fun persistSearchItems(results: List<ProductItemEntity>)
    suspend fun getSearchHistory(): List<String>
    suspend fun deleteSearch(search: SearchEntity)
    suspend fun persistSearch(search: SearchEntity)
    suspend fun deleteSearchItems(results: List<ProductItemEntity>)
    suspend fun findItemsByLink(link: String) : List<ProductItemEntity>
    suspend fun updateItemByLink(detailsUrl: String, itemEntity: ProductItemEntity)
}
private val logger = KotlinLogging.logger {}


class SearchCacheRepositoryImpl(private val searchCacheDao: SearchCacheDao) :
    SearchCacheRepository {

    override suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int, limit: Int ): SearchWithItemsEntity?  {

        logger.debug { "SearchCacheRepositoryImpl::findBySearchTerm" }
        val search = searchCacheDao.findSearch(searchTerm)
        if(search!=null) {
            val resultItemEntities = searchCacheDao.findSearchResultsBySearchId(
                search.searchId,
                limit,
                (page - 1) * limit
            )
            return SearchWithItemsEntity(search = search, results =  resultItemEntities)
        }
        return null

    }

    override suspend fun persistsSearchWithItems(results: SearchWithItemsEntity) {

        val searchId = searchCacheDao.persistSearch(results.search)
        results.results.forEach { it.searchId = searchId.toInt() }

        searchCacheDao.persistItems(results.results)
    }

    override suspend fun persistSearchItems(results: List<ProductItemEntity>) {
        searchCacheDao.persistItems(results)
    }

    override suspend fun getSearchHistory(): List<String> {
        return searchCacheDao.getSearchHistory()
    }

    override suspend fun deleteSearch(search: SearchEntity) {
        searchCacheDao.removeSearch(search)
    }

    override suspend fun persistSearch(search: SearchEntity) {
        searchCacheDao.persistSearch(search)
    }

    override suspend fun deleteSearchItems(results: List<ProductItemEntity>) {
        searchCacheDao.removeItems(results)
    }

    override suspend fun findItemsByLink(link: String): List<ProductItemEntity> {
        return searchCacheDao.findItemsByLink(link)
    }

    override suspend fun updateItemByLink(
        detailsUrl: String,
        itemEntity: ProductItemEntity
    ) {
        //alle search items mit diesem link mit den daten aus der entity aktualisiern
        searchCacheDao.updateItemsByLink(detailsUrl, price = itemEntity.price, priceTrend = itemEntity.priceTrend, lastUpdated = itemEntity.lastUpdated)
    }

}