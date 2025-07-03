package de.dkutzer.tcgwatcher.collectables.history.domain

interface SearchCacheRepository {

    suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchWithBasicProductsInfo?
    suspend fun findSearchWithItemsAndSellOffersByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchWithFullProductInfo?
    suspend fun findProductWithSellOffersByExternalId(externalId: String) : ProductWithSellOffers?
    suspend fun persistsSearchWithItems(searchWithBasicProductsInfo: SearchWithBasicProductsInfo, language: String): SearchWithBasicProductsInfo
    suspend fun persistSearchWithProductAndSellOffers(searchWithProducts: SearchWithFullProductInfo, language: String) : SearchWithFullProductInfo

    suspend fun getProductsByExternalId(externalId: String) : ProductWithSellOffers?

    suspend fun persistSearchItems(results: List<ProductEntity>)
    suspend fun getSearchHistory(): List<String>
    suspend fun deleteSearch(search: SearchEntity)
    suspend fun persistSearch(search: SearchEntity)
    suspend fun deleteSearchItems(results: List<ProductEntity>)
    suspend fun findItemsByLink(link: String) : List<ProductEntity>
    suspend fun updateItemByLink(detailsUrl: String, itemEntity: ProductEntity, names: List<ProductNameEntity> = emptyList(), sets: List<ProductSetEntity> = emptyList())
    suspend fun updateProduct(productWithSellOffers: ProductWithSellOffers)
    suspend fun findSearchWithProductsNamesAndSetsByQuery(searchTerm: String, page: Int = 1, limit: Int = 5): SearchWithFullProductInfo?
}