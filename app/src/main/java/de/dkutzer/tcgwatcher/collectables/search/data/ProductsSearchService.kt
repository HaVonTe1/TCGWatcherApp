package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithProducts
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithProductsAndSellOffers
import de.dkutzer.tcgwatcher.collectables.search.data.cardmarket.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductSearchService
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductsApiClient
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPage
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto
import de.dkutzer.tcgwatcher.settings.domain.BaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class ProductsSearchServiceFactory(private val client: ProductsApiClient, private val cache: SearchCacheRepository, private val config: BaseConfig) {
    fun create(): ProductSearchService {
        if(client is BaseCardmarketApiClient)
            return CardmarketProductSearchService.createInstance(client, cache, config)
        throw  UnsupportedOperationException("Client type not supported")
    }
}


class CardmarketProductSearchService
    private constructor(override val client: ProductsApiClient, override val cache: SearchCacheRepository, override val config: BaseConfig) : ProductSearchService {
    companion object {
        fun createInstance(client: ProductsApiClient, cache: SearchCacheRepository, config: BaseConfig): ProductSearchService {
            return CardmarketProductSearchService(client, cache, config)
        }
    }

    /*
    product is a quicksearchitem - so a previously saved search might exists
     */
    override suspend fun loadQuicksearchProductIntoResultPage(
        product: ProductModel,
        language: String
    ): SearchResultsPage {

        logger.debug { "CardmarketProductSearchService: loadQuicksearchProductIntoResultPage: $product" }

        val searchAndProductsAndSelloffersEntity =
            cache.findSearchWithItemsAndSellOffersByQuery(product.detailsUrl)
        if (searchAndProductsAndSelloffersEntity != null) {
            logger.debug { "Adapter: Returning cached results: $searchAndProductsAndSelloffersEntity" }
            return SearchResultsPage(
                searchAndProductsAndSelloffersEntity.productWithSellOffers.map { it.toProductModel(language) },
                1,
                1)
        }
        val searchEntity = createAndPersistSearchEntity(product, true, language)
        val productModel = searchEntity.productWithSellOffers.first().toProductModel(language)
        val result = SearchResultsPage(listOf(productModel), 1, 1)

        logger.debug { "Adapter: Finally returning results: $result" }
        return result
    }

    /*
    UseCases:
    'product' is an item of a previous search so no need to create a new search entity.
    - a search by query was performed and an item is shown in the details view
        --> the item loaded only minimal data but now all details are needed BUT it should be fast so: cacheOnly = true
    - an item is shown in the details view and no details have been available so far but we want to load all details now: cacheOnly = false
     */
    override suspend fun refreshProduct(product: ProductModel, cacheOnly: Boolean, language: String): ProductModel {
        logger.debug { "CardmarketProductSearchService: getProductWithDetails: $product" }
        logger.debug { "CardmarketProductSearchService: useCache $cacheOnly"}

        val refreshedProduct: ProductModel?  = if (cacheOnly) {
            val product =
                cache.findProductWithSellOffersByExternalId(product.externalId)
            product?.toProductModel(language)

        } else {
            val productDetailsDto = client.getProductDetails(product.detailsUrl)

            //TODO: refactor result type to single entity after refactoring of N:M relation between search and item is done
            val cachedProduct = cache.getProductsByExternalId(product.externalId)
            if (cachedProduct != null) {
                logger.debug { "CardmarketProductSearchService: updating product: $cachedProduct" }
                val updatedProduct = productDetailsDto.toProduct(
                    language,
                    /*searchId*/ 0L, // searchId entfällt, da M:N
                    cachedProduct.productEntity.id
                )
                cache.updateProduct(updatedProduct)
            }

            productDetailsDto.toProductModel(language)
        }

        if(refreshedProduct==null){
            logger.warn { "CardmarketProductSearchService: error while refeshing product: $product" }
        }
        return refreshedProduct ?: product

    }


    private suspend fun createAndPersistSearchEntity(product: ProductModel, loadDetails: Boolean, language: String): SearchWithProductsAndSellOffers {
        val productWithDetails = if(loadDetails) {
            enrichProductWithDetails(product, language)
        } else {
            product
        }
        logger.debug { "Adapter: Persisting enriched product in the cache: $productWithDetails" }

        return SearchWithProductsAndSellOffers(
            search = SearchEntity(
                searchTerm = productWithDetails.detailsUrl,
                size = 1,
                lastUpdated = Instant.now().epochSecond,
                language = config.lang.name,
                history = false
            ),
            productWithSellOffers = listOf(productWithDetails.toProductWithSellofferEntity())
        ).also {
            cache.persistSearchWithProductAndSellOffers(it, config.lang.name)
        }
    }

    private suspend fun enrichProductWithDetails(productModel: ProductModel, language: String) : ProductModel {
        val productDetails = client.getProductDetails(productModel.detailsUrl)
        val productModel = productDetails.toProductModel(language)
        return productModel
    }

    override suspend fun searchByPage(searchString: String, page: Int, limit: Int): SearchResultsPage {
        logger.debug { "Adapter: New Search by searchTerm: $searchString" }
        if(searchString.isEmpty())
            return SearchResultsPage(emptyList(), 1, 1)
        lateinit var result: SearchResultsPage
        logger.debug { "Adapter: Looking in the Cache for: $searchString" }

        var searchWithResults = cache.findSearchWithItemsByQuery(searchString, page)
        logger.debug { "Adapter: Found: ${searchWithResults?.products?.size}" }
        val lastUpdated = searchWithResults?.search?.lastUpdated
        if(lastUpdated!=null)
            logger.debug { "TTL: ${Instant.ofEpochSecond(lastUpdated)}" }

        if(searchWithResults!=null && searchWithResults.isOlderThan(config.ttlInSeconds)) {
            logger.debug { "Adapter: Cache is older than 3 days: ${Instant.ofEpochMilli(lastUpdated!!)}" }

            cache.deleteSearchItems(searchWithResults.products)
            cache.deleteSearch(searchWithResults.search)
            searchWithResults = null
        }
        if(searchWithResults!=null && !searchWithResults.isOlderThan(config.ttlInSeconds)) {
            val searchItems = searchWithResults.products.map { it.toProductModel() }
            result = SearchResultsPage(
                searchItems,
                page,
                searchWithResults.search.size.floorDiv(limit).plus(1)
            )
            logger.debug { "Adapter: Returning cached results: $result" }
        }
        else {
            logger.debug { "Adapter: Start a new Search: $searchString" }
            val duration = measureTimeMillis {

                logger.debug { "Adapter: Requesting the Api with $searchString for page: 1" }
                var searchResults = client.search(searchString, 1)
                var mergedResults = SearchResultsPageDto(
                    searchResults.results,
                    1,
                    searchResults.totalPages
                )
                logger.trace { "Adapter: Results so far: $mergedResults" }
                while (searchResults.page < searchResults.totalPages) {
                    logger.debug { "Adapter: traversing pagination with total pages: ${searchResults.totalPages}" }
                    val newPage = searchResults.page + 1
                    logger.trace { "Adapter: Requesting the Api with $searchString for page: $newPage" }

                    searchResults = client.search(searchString, newPage)

                    mergedResults = SearchResultsPageDto(
                        mergedResults.results.plus(searchResults.results),
                        newPage,
                        searchResults.totalPages
                    )
                    logger.trace { "Adapter: Results so far: $mergedResults" }
                }

                val searchWithProducts = SearchWithProducts(
                    search = SearchEntity(
                        searchTerm = searchString,
                        size = mergedResults.results.size,
                        lastUpdated = Instant.now().epochSecond,
                        language = config.lang.name,
                        history = true
                    ),
                    products = mergedResults.results.map { it.toProductItemEntity() }.toList()
                )
                logger.debug { "Persisting cache: $searchWithProducts" }
                cache.persistsSearchWithItems(searchWithProducts, config.lang.name)

                logger.debug { "Now fetching paged results from newly cache" }
                val updatedSearchResult = cache.findSearchWithItemsByQuery(searchString, page)


                val searchItems = updatedSearchResult?.products?.map { it.toProductModel() }
                result = SearchResultsPage(
                    searchItems ?: listOf(),
                    page,
                    updatedSearchResult?.search?.size?.floorDiv(limit)?.plus(1) ?: 0
                )
            }
            logger.debug { "Duration: $duration" }
        }
        logger.debug { "Final result: $result" }
        logger.info { "Adapter: Returning results: ${result.items.size}" }
        return result
    }
}