package de.dkutzer.tcgwatcher.collectables.history.data

import de.dkutzer.tcgwatcher.collectables.history.domain.ProductNameEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductSetEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchProductCrossRef
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithBasicProductsInfo
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithFullProductInfo
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithProducts
import de.dkutzer.tcgwatcher.collectables.history.domain.SellOfferEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant


private val logger = KotlinLogging.logger {}


class SearchCacheRepositoryImpl(private val searchCacheDao: SearchCacheDao) :
    SearchCacheRepository {

    override suspend fun getSearchWithBasicProductsByQuery(searchTerm: String, page: Int, limit: Int ): SearchWithBasicProductsInfo?  {
        logger.debug { "SearchCacheRepositoryImpl::findBySearchTerm" }
        searchCacheDao.getSearchByTerm(searchTerm)?.let {
             val productComposites =
                 searchCacheDao.getProductsCompositeBySearchId(it.id, limit, (page - 1) * limit)

            return SearchWithBasicProductsInfo(
                search = it,
                basicProducts = productComposites
            )
        }
        return null
    }


    override suspend fun getProductsByExternalId(externalId: String): ProductWithSellOffers? {
        return searchCacheDao.getProductWithSellOffersByProductId(externalId)
    }

    override suspend fun persistProductWithSellOffers(productWithSellOffers: ProductWithSellOffers) {

        val productId = searchCacheDao.upsertProduct(productWithSellOffers.productEntity)
        val newProductID = if(productId == -1L) productWithSellOffers.productEntity.id else productId.toInt()
        productWithSellOffers.names.forEach { it.productId = newProductID}
        searchCacheDao.persistNamesForProduct(productWithSellOffers.names)
        productWithSellOffers.set?.let { searchCacheDao.upsertProductSet(it) }
        productWithSellOffers.offers.forEach { it.productId = newProductID }
        searchCacheDao.upsertSellOffers(productWithSellOffers.offers)
    }

    override suspend fun getSearchWithFullProductsByQuery(searchTerm: String, page: Int, limit: Int ): SearchWithFullProductInfo?  {
        logger.debug { "SearchCacheRepositoryImpl::findBySearchTerm" }
        searchCacheDao.getSearchByTerm(searchTerm)?.let {
            val productComposites =
                searchCacheDao.getProductsWithSellOffersBySearchId(it.id, limit, (page - 1) * limit)

            return SearchWithFullProductInfo(
                search = it,
                fullProducts = productComposites
            )
        }
        return null
    }

    override suspend fun getFullProductInfoByExternalId(externalId: String): ProductWithSellOffers? {
        logger.debug { "SearchCacheRepositoryImpl::findSearchWithItemsAndSellOffersByCmId" }
        return  searchCacheDao.getProductWithSellOffersByProductId(externalId)
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
            searchId = searchCacheDao.upsertSearch(initialSearchEntity).toInt()
            val updatedSearchEntity = initialSearchEntity.copy(id = searchId)
            return Pair(updatedSearchEntity, searchId)
        }

        return Pair(initialSearchEntity, searchId)
    }


    override suspend fun persistSearchWithProducts(
        searchWithProducts: SearchWithProducts,
        language: String
    ) {
        val (searchEntity, searchId) = processSearch(
            searchTerm = searchWithProducts.search.searchTerm,
            productsSize = searchWithProducts.products.size,
            language = language,
            history = searchWithProducts.search.history
        )

        // Bulk insert products
        val productIds =
            searchCacheDao.upsertProducts(searchWithProducts.products.map { it.productEntity })

        // Prepare bulk data for related entities
        val allOffers = mutableListOf<SellOfferEntity>()
        val allNames = mutableListOf<ProductNameEntity>()
        val allSets = mutableListOf<ProductSetEntity>()
        val crossRefs = mutableListOf<SearchProductCrossRef>()

        searchWithProducts.products.forEachIndexed { index, product ->
            val productId = productIds[index].toInt()

            if(product is ProductWithSellOffers) {
                // Collect offers with updated productId
                allOffers.addAll(product.offers.map { it.copy(productId = productId) })
            }

            // Collect names with updated productId
            allNames.addAll(product.names.map { it.copy(productId = productId) })

            // Collect sets with updated productId
            product.set?.let { allSets.add(it.copy(productId = productId)) }

            // Collect cross references
            crossRefs.add(SearchProductCrossRef(searchId = searchId, productId = productId))

        }

        // Bulk insert all related entities
        if(allOffers.isNotEmpty()) {
            searchCacheDao.upsertSellOffers(allOffers)
        }
        if (allNames.isNotEmpty()) {
            searchCacheDao.insertProductNames(allNames)
        }
        if (allSets.isNotEmpty()) {
            searchCacheDao.upsertProductSets(allSets)
        }
        searchCacheDao.insertSearchProductCrossRefs(crossRefs)

    }

    override suspend fun removeProductsFromSearch(
        search: SearchEntity) {
        searchCacheDao.deleteCrossRefsBySearchId(search.id)
    }


    override suspend fun getSearchHistory(): List<String> {
        return searchCacheDao.getSearchHistory()
    }


    override suspend fun persistSearch(search: SearchEntity) {
        searchCacheDao.upsertSearch(search)
    }


}