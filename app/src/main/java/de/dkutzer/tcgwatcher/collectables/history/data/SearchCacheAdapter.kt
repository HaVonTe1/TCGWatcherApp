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
import de.dkutzer.tcgwatcher.collectables.history.domain.SellOfferEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant


private val logger = KotlinLogging.logger {}


class SearchCacheRepositoryImpl(private val searchCacheDao: SearchCacheDao) :
    SearchCacheRepository {

    override suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int, limit: Int ): SearchWithBasicProductsInfo?  {
        logger.debug { "SearchCacheRepositoryImpl::findBySearchTerm" }
        return searchCacheDao.getSearchWithProducts(searchTerm, limit, (page - 1) * limit)
    }


    override suspend fun getProductsByExternalId(externalId: String): ProductWithSellOffers? {
        return searchCacheDao.findItemWithSellOffersByProductId(externalId)
    }

    override suspend fun updateProduct(productWithSellOffers: ProductWithSellOffers) {

        val productId = searchCacheDao.saveItem(productWithSellOffers.productEntity)
        val newProductID = if(productId == -1L) productWithSellOffers.productEntity.id else productId.toInt()
        productWithSellOffers.offers.forEach { it.productId = newProductID }
        searchCacheDao.saveSellOffers(productWithSellOffers.offers)
    }

    override suspend fun findSearchWithItemsAndSellOffersByQuery(searchTerm: String, page: Int, limit: Int ): SearchWithFullProductInfo?  {
        logger.debug { "SearchCacheRepositoryImpl::findBySearchTerm" }
        searchCacheDao.getSearchWithProductsAndSellOffers(searchTerm, page, limit)

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


    override suspend fun persistSearchWithBasicProductsInfo(
        searchWithBasicProductsInfo: SearchWithBasicProductsInfo,
        language: String
    ): SearchWithBasicProductsInfo {
        val (searchEntity, searchId) = processSearch(
            searchTerm = searchWithBasicProductsInfo.search.searchTerm,
            productsSize = searchWithBasicProductsInfo.products.size,
            language = language,
            history = searchWithBasicProductsInfo.search.history
        )
        // Produkte speichern (nur einmalig)
        searchCacheDao.saveItems(searchWithBasicProductsInfo.products)
        // CrossRefs anlegen
        val crossRefs = searchWithBasicProductsInfo.products.map { product ->
            SearchProductCrossRef(searchId = searchId, productId = product.id)
        }
        searchCacheDao.insertSearchProductCrossRefs(crossRefs)
        return SearchWithBasicProductsInfo(searchEntity, searchWithBasicProductsInfo.products)
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
            searchCacheDao.saveItems(searchWithProducts.products.map { it.productEntity })

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
        searchCacheDao.saveSellOffers(allOffers)
        if (allNames.isNotEmpty()) {
            searchCacheDao.insertProductNames(allNames)
        }
        if (allSets.isNotEmpty()) {
            searchCacheDao.insertProductSets(allSets)
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


    /*
    This function removes the products from the searchProdcuts CrossReference. 
    The Product Entities are NOT deleted.
     */
    override suspend fun removeProductsFromSearch(
        search: SearchEntity,
        products: List<ProductEntity>
    ) {
        val crossRefsToDelete = products.map { product ->
            SearchProductCrossRef(searchId = search.id, productId = product.id)
        }
        searchCacheDao.deleteSearchProductCrossRefs(crossRefsToDelete)
    }

    override suspend fun persistProducts(results: List<ProductEntity>) {
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

    override suspend fun deleteProducts(results: List<ProductEntity>) {
        searchCacheDao.removeItems(results)
    }

    override suspend fun findProductsByLink(link: String): List<ProductEntity> {
        return searchCacheDao.findItemsByLink(link)
    }

    // Hilfsmethoden für Namen und Sets
    suspend fun saveProductNames(names: List<ProductNameEntity>) {
        searchCacheDao.insertProductNames(names)
    }
    suspend fun saveProductSets(sets: List<ProductSetEntity>) {
        searchCacheDao.insertProductSets(sets)
    }
    suspend fun getProductNames(productId: Int): List<ProductNameEntity> {
        return searchCacheDao.getProductNames(productId)
    }
    suspend fun getProductSets(productId: Int): List<ProductSetEntity> {
        return searchCacheDao.getProductSets(productId)
    }

    override suspend fun updateProductByDetailsUrl(
        detailsUrl: String,
        itemEntity: ProductEntity,
        names: List<ProductNameEntity>,
        sets: List<ProductSetEntity>
    ) {
        // Produkt aktualisieren (z.B. Preis, Trend, Rarität, Typ, etc.)
        searchCacheDao.saveItem(itemEntity)
        // Namen aktualisieren, falls übergeben
        if (names.isNotEmpty()) {
            searchCacheDao.insertProductNames(names)
        }
        // Sets aktualisieren, falls übergeben
        if (sets.isNotEmpty()) {
            searchCacheDao.insertProductSets(sets)
        }
    }

    override suspend fun findSearchWithProductsNamesAndSetsByQuery(searchTerm: String, page: Int, limit: Int): SearchWithFullProductInfo? {
        logger.debug { "SearchCacheRepositoryImpl::findSearchWithProductsNamesAndSetsByQuery" }
        return  searchCacheDao.getSearchWithProductsAndSellOffers(searchTerm, limit, (page - 1) * limit)

    }

}