package de.dkutzer.tcgwatcher.collectables.history.data

import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithItemsEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant


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
            return SearchWithItemsEntity(search = search, products =  resultItemEntities)
        }
        return null

    }

    override suspend fun persistsSearchWithItems(results: SearchWithItemsEntity): SearchWithItemsEntity {

        var searchId = searchCacheDao.getSearchIdBySearchTerm(results.search.searchTerm)
        val lastUpdated = Instant.now().epochSecond
        if(searchId!=null) {

            searchCacheDao.updateLastUpdated(searchId, lastUpdated)

        } else
        {
            val persistedSearchId = searchCacheDao.persistSearch(results.search)
            results.products.forEach { it.searchId = persistedSearchId.toInt() }

            searchCacheDao.persistItems(results.products)
            searchId = persistedSearchId.toInt()
        }
        return SearchWithItemsEntity(
            SearchEntity(
                searchId =  searchId,
                searchTerm = results.search.searchTerm,
                lastUpdated = lastUpdated,
                size = results.products.size,
                history = results.search.history),
            results.products)
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
        searchCacheDao.updateItemsByLink(
            detailsUrl = detailsUrl,
            price = itemEntity.price,
            priceTrend = itemEntity.priceTrend,
            orgName = itemEntity.orgName,
            setName = itemEntity.setName,
            setLink = itemEntity.setLink,
            rarity = itemEntity.rarity,
            type = itemEntity.type,
            lastUpdated = itemEntity.lastUpdated)
    }

}