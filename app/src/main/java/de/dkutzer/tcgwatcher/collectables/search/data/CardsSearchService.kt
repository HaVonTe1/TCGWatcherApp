package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.history.domain.SearchAndProductsAndSelloffersEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchAndProductsEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchCacheRepository
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.search.domain.CardsSearchService
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPage
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}


class CardmarketCardsSearchServiceAdapter(
    private val client: BaseCardmarketApiClient,
    private val cache: SearchCacheRepository
) : CardsSearchService {

    private val threeDaysSeconds = 3 * 24 * 60 * 60L //TODO: make it configurable


    override suspend fun getSingleItemByItem(product: ProductModel, useCache: Boolean, useTtl:Boolean, loadDetails: Boolean, language: String): SearchResultsPage {

        /*
        This use case is different from the search by query.
        We might dont have a "searchEntity" here. Because we came here via a refresh from a singleItemView.
        Only in the case, that we already refreshed this item, where is a "SearchEntity".
        In every case the SearchEntity with the cmLink as Id needs to pe upserted.
         */
        logger.debug { "Adapter: getSingleItemByLink: $product" }
        logger.debug { "Adapter: useCache $useCache useTtl $useTtl"}

        val searchWithItemsEntity = if(useCache) {
            logger.debug { "Adapter: Looking in the Cache for: ${product.detailsUrl}" }
            val cachedSearch = cache.findSearchWithItemsAndSellOffersByQuery(product.detailsUrl)
            logger.debug { "Adapter: Found: $cachedSearch" }

            if (cachedSearch == null || (useTtl && cachedSearch.isOlderThan(threeDaysSeconds))) {
                logger.debug { "Adapter: Cache is older than 3 days: ${Instant.ofEpochMilli(cachedSearch?.search?.lastUpdated!!)}" }
                createAndPersistSearchEntity(product,loadDetails, language)
            } else {
                logger.debug { "Adapter: Returning cached results: $cachedSearch" }
                cachedSearch
            }
        } else {
            logger.debug { "Adapter: no cache - Start a new Search: ${product.detailsUrl}" }
            createAndPersistSearchEntity(product, loadDetails, language)
        }
        val productModel = searchWithItemsEntity.products.first().toProductModel()
        val result = SearchResultsPage(listOf(productModel), 1, 1)

        cache.updateItemByLink(product.detailsUrl, productModel.toProductItemEntity())

        logger.debug { "Adapter: Finally returning results: $result" }
        return result
    }


    private suspend fun createAndPersistSearchEntity(product: ProductModel, loadDetails: Boolean, language: String): SearchAndProductsAndSelloffersEntity {
        val productWithDetails = if(loadDetails) {
            enrichProductWithDetails(product, language)
        } else {
            product
        }
        logger.debug { "Adapter: Persisting enriched product in the cache: $productWithDetails" }

        return SearchAndProductsAndSelloffersEntity(
            search = SearchEntity(
                searchTerm = productWithDetails.detailsUrl,
                size = 1,
                lastUpdated = Instant.now().epochSecond,
                language = language,
                history = false
            ),
            products = listOf(productWithDetails.toProductWithSellofferEntity())
        ).also {
            cache.persistSearchWithProductAndSellOffers(it, language)
        }
    }

    private suspend fun enrichProductWithDetails(searchItem: ProductModel, language: String) : ProductModel {
        val productDetails = client.getProductDetails(searchItem.detailsUrl)
        val productModel = productDetails.toProductModel(language)
        return productModel
    }

    override suspend fun searchByPage(searchString: String, page: Int, limit: Int, language: String): SearchResultsPage {
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

        if(searchWithResults!=null && searchWithResults.isOlderThan(threeDaysSeconds)) {
            logger.debug { "Adapter: Cache is older than 3 days: ${Instant.ofEpochMilli(lastUpdated!!)}" }

            cache.deleteSearchItems(searchWithResults.products)
            cache.deleteSearch(searchWithResults.search)
            searchWithResults = null
        }
        if(searchWithResults!=null && !searchWithResults.isOlderThan(threeDaysSeconds)) {
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

                val searchAndProductsEntity = SearchAndProductsEntity(
                    search = SearchEntity(
                        searchTerm = searchString,
                        size = mergedResults.results.size,
                        lastUpdated = Instant.now().epochSecond,
                        language = language,
                        history = true
                    ),
                    products = mergedResults.results.map { it.toProductItemEntity() }.toList()
                )
                logger.debug { "Persisting cache: $searchAndProductsEntity" }
                cache.persistsSearchWithItems(searchAndProductsEntity, language)

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