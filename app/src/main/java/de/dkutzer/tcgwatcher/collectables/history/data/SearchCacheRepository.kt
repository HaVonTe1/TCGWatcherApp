package de.dkutzer.tcgwatcher.collectables.history.data

import de.dkutzer.tcgwatcher.collectables.history.domain.Product
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
                search.id,
                limit,
                (page - 1) * limit
            )
            return SearchAndProductsEntity(search = search, products =  resultItemEntities)
        }
        return null

    }

    override suspend fun findSearchWithItemsAndSellOffersByQuery(searchTerm: String, page: Int, limit: Int ): SearchAndProductsAndSelloffersEntity?  {
        logger.debug { "SearchCacheRepositoryImpl::findBySearchTerm" }
        val search = searchCacheDao.findSearch(searchTerm)
        if(search!=null) {
            val resultItemEntities = searchCacheDao.findSearchResultsBySearchId(
                search.id,
                limit,
                (page - 1) * limit
            )
            val resultItemsWithSellOffersEntities = resultItemEntities.map {
                val sellOffers = searchCacheDao.findSellOfferByProductId(it.id)
                Product(it, sellOffers)
            }

            return SearchAndProductsAndSelloffersEntity(search = search, products =  resultItemsWithSellOffersEntities)
        }
        return null

    }

    fun processSearch(
        searchTerm: String,
        productsSize: Int,
        language: String,
        history: Boolean
    ): Pair<SearchEntity, Int> {
        val currentEpochTime = Instant.now().epochSecond
        var searchId = searchCacheDao.getSearchIdBySearchTerm(searchTerm)

        val initialSearchEntity = SearchEntity(
            id = searchId ?: 0,
            searchTerm = searchTerm,
            lastUpdated = currentEpochTime,
            size = productsSize,
            language = language,
            history = history
        )

        if (searchId != null) {
            searchCacheDao.updateSearch(initialSearchEntity)
        } else {
            searchId = searchCacheDao.persistSearch(initialSearchEntity).toInt()
            val updatedSearchEntity = initialSearchEntity.copy(id = searchId)
            return Pair(updatedSearchEntity, searchId)
        }

        return Pair(initialSearchEntity, searchId)
    }


    override suspend fun persistsSearchWithItems(
        searchWithProducts: SearchAndProductsEntity,
        language: String
    ): SearchAndProductsEntity {
        val (searchEntity, searchId) = processSearch(
            searchTerm = searchWithProducts.search.searchTerm,
            productsSize = searchWithProducts.products.size,
            language = language,
            history = searchWithProducts.search.history
        )

        searchWithProducts.products.forEach { it.searchId = searchId }
        searchCacheDao.persistItems(searchWithProducts.products)

        return SearchAndProductsEntity(searchEntity, searchWithProducts.products)
    }


    override suspend fun persistSearchWithProductAndSellOffers(
        searchWithProducts: SearchAndProductsAndSelloffersEntity,
        language: String
    ): SearchAndProductsAndSelloffersEntity {
        val (searchEntity, searchId) = processSearch(
            searchTerm = searchWithProducts.search.searchTerm,
            productsSize = searchWithProducts.products.size,
            language = language,
            history = searchWithProducts.search.history
        )

        searchWithProducts.products.forEach { product ->
            product.productItemEntity.searchId = searchId
            val productId = searchCacheDao.persistItem(product.productItemEntity)
            product.offers.forEach { it.productId = productId.toInt() }
            searchCacheDao.persistSellOffers(product.offers)
        }

        return SearchAndProductsAndSelloffersEntity(searchEntity, searchWithProducts.products)
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