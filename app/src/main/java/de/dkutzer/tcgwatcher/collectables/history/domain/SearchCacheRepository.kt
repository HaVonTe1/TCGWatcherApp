package de.dkutzer.tcgwatcher.collectables.history.domain

interface SearchCacheRepository {

    suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchWithMinimalProducts?
    suspend fun findSearchWithItemsAndSellOffersByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchWithProductsAndSellOffers?
    suspend fun findProductWithSellOffersByExternalId(externalId: String) : ProductAggregate?
    suspend fun persistsSearchWithItems(searchWithMinimalProducts: SearchWithMinimalProducts, language: String): SearchWithMinimalProducts
    suspend fun persistSearchWithProductAndSellOffers(searchWithProducts: SearchWithProductsAndSellOffers, language: String) : SearchWithProductsAndSellOffers

    suspend fun getProductsByExternalId(externalId: String) : ProductAggregate?

    suspend fun persistSearchItems(results: List<ProductEntity>)
    suspend fun getSearchHistory(): List<String>
    suspend fun deleteSearch(search: SearchEntity)
    suspend fun persistSearch(search: SearchEntity)
    suspend fun deleteSearchItems(results: List<ProductEntity>)
    suspend fun findItemsByLink(link: String) : List<ProductEntity>
    suspend fun updateItemByLink(detailsUrl: String, itemEntity: ProductEntity, names: List<ProductNameEntity> = emptyList(), sets: List<ProductSetEntity> = emptyList())
    suspend fun updateProduct(productAggregate: ProductAggregate)
    suspend fun findSearchWithProductsNamesAndSetsByQuery(searchTerm: String, page: Int = 1, limit: Int = 5): SearchWithFullProductInfo?
}