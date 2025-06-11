package de.dkutzer.tcgwatcher.collectables.history.data

import de.dkutzer.tcgwatcher.collectables.history.domain.ProductEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithProducts
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithProductsAndSellOffers
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant


private val logger = KotlinLogging.logger {}


class SearchCacheRepositoryImpl(private val searchCacheDao: SearchCacheDao) :
    SearchCacheRepository {

    override suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int, limit: Int ): SearchWithProducts?  {

        logger.debug { "SearchCacheRepositoryImpl::findBySearchTerm" }
        val search = searchCacheDao.findSearch(searchTerm)
        if(search!=null) {
            val resultItemEntities = searchCacheDao.findSearchResultsBySearchId(
                search.id,
                limit,
                (page - 1) * limit
            )
            return SearchWithProducts(search = search, products =  resultItemEntities)
        }
        return null

    }

    //TODO: refactor result type to single entity after refactoring of N:M relation between search and item is done
    override suspend fun getProductsByExternalId(externalId: String): List<ProductWithSellOffers> {

       return  searchCacheDao.findProductsByExternalId(externalId)

    }

    override suspend fun updateProduct(productWithSellOffers: ProductWithSellOffers) {

        val productId = searchCacheDao.saveItem(productWithSellOffers.productEntity)
        productWithSellOffers.offers.forEach { it.productId = productId.toInt() }
        searchCacheDao.saveSellOffers(productWithSellOffers.offers)
    }

    override suspend fun findSearchWithItemsAndSellOffersByQuery(searchTerm: String, page: Int, limit: Int ): SearchWithProductsAndSellOffers?  {
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
                ProductWithSellOffers(it, sellOffers)
            }

            return SearchWithProductsAndSellOffers(search = search, productWithSellOffers =  resultItemsWithSellOffersEntities)
        }
        return null

    }

    override suspend fun findProductWithSellOffersByExternalId(externalId: String): ProductWithSellOffers? {
        logger.debug { "SearchCacheRepositoryImpl::findSearchWithItemsAndSellOffersByCmId" }
        val product = searchCacheDao.findItemWithSellOffersByProductId(externalId)

        return product
    }

    fun processSearch(
        searchTerm: String,
        productsSize: Int,
        language: String,
        history: Boolean
    ): Pair<SearchEntity, Int> {
        val currentEpochTime = Instant.now().epochSecond
        var searchId = searchCacheDao.findSearchIdBySearchTerm(searchTerm)

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
            searchId = searchCacheDao.saveSearch(initialSearchEntity).toInt()
            val updatedSearchEntity = initialSearchEntity.copy(id = searchId)
            return Pair(updatedSearchEntity, searchId)
        }

        return Pair(initialSearchEntity, searchId)
    }


    override suspend fun persistsSearchWithItems(
        searchWithProducts: SearchWithProducts,
        language: String
    ): SearchWithProducts {
        val (searchEntity, searchId) = processSearch(
            searchTerm = searchWithProducts.search.searchTerm,
            productsSize = searchWithProducts.products.size,
            language = language,
            history = searchWithProducts.search.history
        )

        searchWithProducts.products.forEach { it.searchId = searchId }
        searchCacheDao.saveItems(searchWithProducts.products)

        return SearchWithProducts(searchEntity, searchWithProducts.products)
    }


    override suspend fun persistSearchWithProductAndSellOffers(
        searchWithProducts: SearchWithProductsAndSellOffers,
        language: String
    ): SearchWithProductsAndSellOffers {
        val (searchEntity, searchId) = processSearch(
            searchTerm = searchWithProducts.search.searchTerm,
            productsSize = searchWithProducts.productWithSellOffers.size,
            language = language,
            history = searchWithProducts.search.history
        )

        val updatedProductWithSellOffers = searchWithProducts.productWithSellOffers.map { product ->
            product.productEntity.searchId = searchId
            val productId = searchCacheDao.saveItem(product.productEntity)
            val updatedProductItemEntity = product.productEntity.copy(id = productId.toInt())
            product.offers.forEach { it.productId = productId.toInt() }
            searchCacheDao.saveSellOffers(product.offers)
            ProductWithSellOffers(updatedProductItemEntity, product.offers)
        }.toList()

        return SearchWithProductsAndSellOffers(searchEntity, updatedProductWithSellOffers)
    }




    override suspend fun persistSearchItems(results: List<ProductEntity>) {
        searchCacheDao.saveItems(results)
    }

    override suspend fun getSearchHistory(): List<String> {
        return searchCacheDao.getSearchHistory()
    }

    override suspend fun deleteSearch(search: SearchEntity) {
        searchCacheDao.removeSearch(search)
    }

    override suspend fun persistSearch(search: SearchEntity) {
        searchCacheDao.saveSearch(search)
    }

    override suspend fun deleteSearchItems(results: List<ProductEntity>) {
        searchCacheDao.removeItems(results)
    }

    override suspend fun findItemsByLink(link: String): List<ProductEntity> {
        return searchCacheDao.findItemsByLink(link)
    }

    override suspend fun updateItemByLink(
        detailsUrl: String,
        itemEntity: ProductEntity
    ) {
        //alle search items mit diesem link mit den daten aus der entity aktualisiern
        searchCacheDao.updateItemsByLink(
            detailsUrl = detailsUrl,
            price = itemEntity.price,
            priceTrend = itemEntity.priceTrend,
            orgName = itemEntity.orgName,
            setName = itemEntity.setName,
            setLink = itemEntity.setId,
            rarity = itemEntity.rarity,
            type = itemEntity.type,
            lastUpdated = itemEntity.lastUpdated)
    }

}