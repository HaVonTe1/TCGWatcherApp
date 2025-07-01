package de.dkutzer.tcgwatcher.collectables.history.data

import de.dkutzer.tcgwatcher.collectables.history.domain.ProductEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductNameEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductSetEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductAggregate
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchProductCrossRef
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithMinimalProducts
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithProductsAndSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithFullProductInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant


private val logger = KotlinLogging.logger {}


class SearchCacheRepositoryImpl(private val searchCacheDao: SearchCacheDao) :
    SearchCacheRepository {

    override suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int, limit: Int ): SearchWithMinimalProducts?  {

        logger.debug { "SearchCacheRepositoryImpl::findBySearchTerm" }
        val search = searchCacheDao.findSearch(searchTerm)
        if(search!=null) {
            // Produkte über Relation laden (Paging ggf. anpassen)
            val relation = getSearchWithProducts(search.id)
            return relation
        }
        return null

    }

    suspend fun getSearchWithProducts(searchId: Int): SearchWithMinimalProducts? {
        return searchCacheDao.getSearchWithProducts(searchId)
    }

    //TODO: refactor result type to single entity after refactoring of N:M relation between search and item is done
    override suspend fun getProductsByExternalId(externalId: String): ProductAggregate? {
        return searchCacheDao.findItemWithSellOffersByProductId(externalId)
    }

    override suspend fun updateProduct(productAggregate: ProductAggregate) {

        val productId = searchCacheDao.saveItem(productAggregate.productEntity)
        val newProductID = if(productId == -1L) productAggregate.productEntity.id else productId.toInt()
        productAggregate.offers.forEach { it.productId = newProductID }
        searchCacheDao.saveSellOffers(productAggregate.offers)
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
                ProductAggregate(it, sellOffers)
            }

            return SearchWithProductsAndSellOffers(search = search, productWithSellOffers =  resultItemsWithSellOffersEntities)
        }
        return null

    }

    override suspend fun findProductWithSellOffersByExternalId(externalId: String): ProductAggregate? {
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
        searchWithMinimalProducts: SearchWithMinimalProducts,
        language: String
    ): SearchWithMinimalProducts {
        val (searchEntity, searchId) = processSearch(
            searchTerm = searchWithMinimalProducts.search.searchTerm,
            productsSize = searchWithMinimalProducts.products.size,
            language = language,
            history = searchWithMinimalProducts.search.history
        )
        // Produkte speichern (nur einmalig)
        searchCacheDao.saveItems(searchWithMinimalProducts.products)
        // CrossRefs anlegen
        val crossRefs = searchWithMinimalProducts.products.map { product ->
            SearchProductCrossRef(searchId = searchId, productId = product.id)
        }
        searchCacheDao.insertSearchProductCrossRefs(crossRefs)
        return SearchWithMinimalProducts(searchEntity, searchWithMinimalProducts.products)
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
            val productId = searchCacheDao.saveItem(product.productEntity)
            val updatedProductItemEntity = product.productEntity.copy(id = productId.toInt())
            product.offers.forEach { it.productId = productId.toInt() }
            searchCacheDao.saveSellOffers(product.offers)
            // CrossRef anlegen
            searchCacheDao.insertSearchProductCrossRefs(listOf(SearchProductCrossRef(searchId = searchId, productId = productId.toInt())))
            ProductAggregate(updatedProductItemEntity, product.offers)
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

    override suspend fun updateItemByLink(
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
        val search = searchCacheDao.findSearch(searchTerm)
        if (search != null) {
            val relation = searchCacheDao.getSearchWithProductsNamesAndSets(search.id)
            return relation
        }
        return null
    }

}