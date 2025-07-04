package de.dkutzer.tcgwatcher.collectables.history.data

import de.dkutzer.tcgwatcher.collectables.history.domain.ProductEntity
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
        return searchCacheDao.getSearchWithProducts(searchTerm, limit, (page - 1) * limit)
    }


    override suspend fun getProductsByExternalId(externalId: String): ProductWithSellOffers? {
        return searchCacheDao.getProductWithSellOffersByProductId(externalId)
    }

    override suspend fun updateProduct(productWithSellOffers: ProductWithSellOffers) {

        val productId = searchCacheDao.upsertProduct(productWithSellOffers.productEntity)
        val newProductID = if(productId == -1L) productWithSellOffers.productEntity.id else productId.toInt()
        productWithSellOffers.offers.forEach { it.productId = newProductID }
        searchCacheDao.upsertSellOffers(productWithSellOffers.offers)
    }

    override suspend fun getSearchWithFullProductsByQuery(searchTerm: String, page: Int, limit: Int ): SearchWithFullProductInfo?  {
        logger.debug { "SearchCacheRepositoryImpl::findBySearchTerm" }
        return searchCacheDao.getSearchWithProductsAndSellOffers(searchTerm, page, limit)
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


    override suspend fun persistSearchWithBasicProductsInfo(
        searchWithProducts: SearchWithProducts,
        language: String
    ): SearchWithBasicProductsInfo {
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

            // Collect offers with updated productId
            allOffers.addAll(product.offers.map { it.copy(productId = productId) })

            // Collect names with updated productId
            allNames.addAll(product.names.map { it.copy(productId = productId) })

            // Collect sets with updated productId
            product.set?.let { allSets.add(it.copy(productId = productId)) }

            // Collect cross references
            crossRefs.add(SearchProductCrossRef(searchId = searchId, productId = productId))
        }

        // Bulk insert all related entities
        searchCacheDao.upsertSellOffers(allOffers)
        if (allNames.isNotEmpty()) {
            searchCacheDao.insertProductNames(allNames)
        }
        if (allSets.isNotEmpty()) {
            searchCacheDao.upsertProductSets(allSets)
        }
        searchCacheDao.insertSearchProductCrossRefs(crossRefs)

    }

    override suspend fun persistSearchWithProductAndSellOffers(
        searchWithProducts: SearchWithFullProductInfo,
        language: String
    ): SearchWithFullProductInfo {
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

            // Collect offers with updated productId
            allOffers.addAll(product.offers.map { it.copy(productId = productId) })

            // Collect names with updated productId
            allNames.addAll(product.names.map { it.copy(productId = productId) })

            // Collect sets with updated productId
            product.set?.let { allSets.add(it.copy(productId = productId)) }

            // Collect cross references
            crossRefs.add(SearchProductCrossRef(searchId = searchId, productId = productId))
        }

        // Bulk insert all related entities
        searchCacheDao.upsertSellOffers(allOffers)
        if (allNames.isNotEmpty()) {
            searchCacheDao.insertProductNames(allNames)
        }
        if (allSets.isNotEmpty()) {
            searchCacheDao.upsertProductSets(allSets)
        }
        searchCacheDao.insertSearchProductCrossRefs(crossRefs)

        // Build updated result
        val updatedProductWithSellOffers =
            searchWithProducts.products.mapIndexed { index, product ->
                val productId = productIds[index].toInt()
            ProductWithSellOffers(
                productEntity = product.productEntity.copy(id = productId),
                offers = product.offers.map { it.copy(productId = productId) },
                names = product.names.map { it.copy(productId = productId) },
                set = product.set?.copy(productId = productId)
            )
        }

        return SearchWithFullProductInfo(searchEntity, updatedProductWithSellOffers)
    }


    override suspend fun removeProductsFromSearch(
        search: SearchEntity) {
        searchCacheDao.deleteCrossRefsBySearchId(search.id)
    }

    override suspend fun persistProducts(results: List<ProductEntity>) {
        searchCacheDao.upsertProducts(results)
    }

    override suspend fun getSearchHistory(): List<String> {
        return searchCacheDao.getSearchHistory()
    }

    override suspend fun deleteSearch(search: SearchEntity) {
        searchCacheDao.deleteSearch(search)
    }

    override suspend fun persistSearch(search: SearchEntity) {
        searchCacheDao.upsertSearch(search)
    }

    override suspend fun deleteProducts(results: List<ProductEntity>) {
        searchCacheDao.deleteProducts(results)
    }

    override suspend fun updateProductByDetailsUrl(
        detailsUrl: String,
        productEntity: ProductEntity,
        names: List<ProductNameEntity>,
        sets: List<ProductSetEntity>
    ) {
        // Produkt aktualisieren (z.B. Preis, Trend, Rarität, Typ, etc.)
        searchCacheDao.upsertProduct(productEntity)
        // Namen aktualisieren, falls übergeben
        if (names.isNotEmpty()) {
            searchCacheDao.insertProductNames(names)
        }
        // Sets aktualisieren, falls übergeben
        if (sets.isNotEmpty()) {
            searchCacheDao.insertProductSets(sets)
        }
    }


}