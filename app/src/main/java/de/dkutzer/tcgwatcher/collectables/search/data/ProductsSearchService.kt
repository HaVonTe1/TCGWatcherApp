package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithBasicProductsInfo
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithFullProductInfo
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
        quickSearchProduct: ProductModel,
        language: String
    ): SearchResultsPage {

        logger.debug { "CardmarketProductSearchService: loadQuicksearchProductIntoResultPage: $quickSearchProduct" }

        val searchAndProductsAndSelloffersEntity =
            cache.getSearchWithFullProductsByQuery(quickSearchProduct.detailsUrl)
        if (searchAndProductsAndSelloffersEntity != null) {
            logger.debug { "Adapter: Returning cached results: $searchAndProductsAndSelloffersEntity" }
            return SearchResultsPage(
                searchAndProductsAndSelloffersEntity.products.map { (it as ProductWithSellOffers).toProductModel(language) },
                1,
                1)
        }
        val searchEntity = createAndPersistSearchEntity(quickSearchProduct, true, language)
        val productModel = (searchEntity.products.first() as ProductWithSellOffers).toProductModel(language)
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
    override suspend fun refreshProduct(productModel: ProductModel, cacheOnly: Boolean, language: String): ProductModel {
        logger.debug { "CardmarketProductSearchService: getProductWithDetails: $productModel" }
        logger.debug { "CardmarketProductSearchService: useCache $cacheOnly"}

        val refreshedProduct: ProductModel?  = if (cacheOnly) {
            val product =
                cache.getFullProductInfoByExternalId(productModel.externalId)
            product?.toProductModel(language)

        } else {
            val productDetailsDto = client.getProductDetails(productModel.detailsUrl)

            val cachedProduct = cache.getProductsByExternalId(productModel.externalId)
            if (cachedProduct != null) {
                logger.debug { "CardmarketProductSearchService: updating product: $cachedProduct" }
                val updatedProduct = productDetailsDto.toProductWithSellOffersEntity(
                    language = language,
                    productId = cachedProduct.productEntity.id
                )
                cache.persistProductWithSellOffers(updatedProduct)
            }

            productDetailsDto.toProductModel(language)
        }

        if(refreshedProduct==null){
            logger.warn { "CardmarketProductSearchService: error while refeshing product: $productModel" }
        }
        return refreshedProduct ?: productModel

    }


    private suspend fun createAndPersistSearchEntity(product: ProductModel, loadDetails: Boolean, language: String): SearchWithFullProductInfo {
        val productWithDetails = if(loadDetails) {
            enrichProductWithDetails(product, language)
        } else {
            product
        }
        logger.debug { "Adapter: Persisting enriched product in the cache: $productWithDetails" }

        return SearchWithFullProductInfo(
            search = SearchEntity(
                searchTerm = productWithDetails.detailsUrl,
                size = 1,
                lastUpdated = Instant.now().epochSecond,
                language = config.lang.name,
                history = false
            ),
            fullProducts = listOf(productWithDetails.toProductWithSellofferEntity())
        ).also {
            cache.persistSearchWithProducts(it, config.lang.name)
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

        var searchWithBasicProdcuts = cache.getSearchWithBasicProductsByQuery(searchString, page, limit)
        logger.debug { "Adapter: Found: ${searchWithBasicProdcuts?.products?.size}" }

        if(searchWithBasicProdcuts!=null && searchWithBasicProdcuts.isOlderThan(config.ttlInSeconds)) {
            logger.debug { "Adapter: Cache is older than 3 days" }
            cache.removeProductsFromSearch(searchWithBasicProdcuts.search)
            searchWithBasicProdcuts = null
        }
        if(searchWithBasicProdcuts!=null && !searchWithBasicProdcuts.isOlderThan(config.ttlInSeconds)) {
            val productModels = searchWithBasicProdcuts.products.map { it.toProductModel(config.lang.localeCode) }.toList()
            result = SearchResultsPage(
                productModels,
                page,
                searchWithBasicProdcuts.search.size.floorDiv(limit).plus(1)
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

                val searchWithBasicProductsInfo = SearchWithBasicProductsInfo(
                    search = SearchEntity(
                        searchTerm = searchString,
                        size = mergedResults.results.size,
                        lastUpdated = Instant.now().epochSecond,
                        language = config.lang.name,
                        history = true
                    ),
                    basicProducts = mergedResults.results.map { it.toProductComposite() }
                )
                logger.debug { "Persisting cache: $searchWithBasicProductsInfo" }
                cache.persistSearchWithProducts(searchWithBasicProductsInfo, config.lang.name)

                logger.debug { "Now fetching paged results from newly cache" }
                val updatedSearchResult = cache.getSearchWithBasicProductsByQuery(searchString, page)


                val searchItems = updatedSearchResult?.products?.map { it.toProductModel(config.lang.localeCode) }
                result = SearchResultsPage(
                    searchItems ?: listOf(),
                    page,
                    updatedSearchResult?.search?.size?.floorDiv(limit)?.plus(1) ?: 0
                )
            }
            logger.debug { "Duration: $duration" }
        }
        logger.debug { "Final result: $result" }
        logger.info { "Adapter: Returning results: ${result.products.size}" }
        return result
    }
}