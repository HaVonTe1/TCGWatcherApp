package de.dkutzer.tcgwatcher.collectables.history.data

import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchAndProductsAndSelloffersEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchAndProductsEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant


private val logger = KotlinLogging.logger {}


class SearchCacheRepositoryImpl(private val searchCacheDao: SearchCacheDao) :
    SearchCacheRepository {

    override suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int, limit: Int ): SearchAndProductsEntity?  {

        logger.debug { "SearchCacheRepositoryImpl::findBySearchTerm" }
        val search = searchCacheDao.findSearch(searchTerm)
        if(search!=null) {
            val resultItemEntities = searchCacheDao.findSearchResultsBySearchId(
                search.searchId,
                limit,
                (page - 1) * limit
            )
            return SearchAndProductsEntity(search = search, products =  resultItemEntities)
        }
        return null

    }


    /*
    TODO: currently every search is persisted with NEW ProductItemEntities.
            Even  the SingleItem Search and the Refreshing of a single item
            leads to a new search entity and even worst a new set of ProductItemEntities.
            This should be optimized. Currently the association between Search and ProductItemEntities is 1:N.
            It should be N:M so every ProductItemEntity has one or more SearchIds and is only persisted once.
    */
    override suspend fun persistsSearchWithItems(searchWithProducts: SearchAndProductsEntity, language: String): SearchAndProductsEntity {

        val currentEpochTime = Instant.now().epochSecond

        var searchIdBySearchTerm =
            searchCacheDao.getSearchIdBySearchTerm(searchWithProducts.search.searchTerm)
        val searchEntity = SearchEntity(
            searchId = searchIdBySearchTerm ?: 0,
            searchTerm = searchWithProducts.search.searchTerm,
            lastUpdated = currentEpochTime,
            size = searchWithProducts.products.size,
            language = language,
            history = searchWithProducts.search.history
        )

        if (searchIdBySearchTerm != null) {
            searchCacheDao.updateSearch(searchEntity)
        } else {
            searchIdBySearchTerm = searchCacheDao.persistSearch(searchWithProducts.search).toInt()
        }

        searchWithProducts.products.forEach { it.searchId = searchIdBySearchTerm }

        searchCacheDao.persistItems(searchWithProducts.products)
        return SearchAndProductsEntity(
            search = searchEntity,
            products = searchWithProducts.products)
    }

    override suspend fun persistSearchWithProductAndSellOffers(
        searchWithProducts: SearchAndProductsAndSelloffersEntity,
        language: String
    ): SearchAndProductsAndSelloffersEntity {
        val currentEpochTime = Instant.now().epochSecond

        var searchIdBySearchTerm =
            searchCacheDao.getSearchIdBySearchTerm(searchWithProducts.search.searchTerm)
        val searchEntity = SearchEntity(
            searchId = searchIdBySearchTerm ?: 0,
            searchTerm = searchWithProducts.search.searchTerm,
            lastUpdated = currentEpochTime,
            size = searchWithProducts.products.size,
            language = language,
            history = searchWithProducts.search.history
        )

        if (searchIdBySearchTerm != null) {
            searchCacheDao.updateSearch(searchEntity)
        } else {
            searchIdBySearchTerm = searchCacheDao.persistSearch(searchWithProducts.search).toInt()
        }

        val list = searchWithProducts.products.map { it.productItemEntity }.onEach {
            it.searchId = searchIdBySearchTerm
        }.toList()


        searchCacheDao.persistItems(list)
        return SearchAndProductsEntity(
            search = searchEntity,
            products = searchWithProducts.products)
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