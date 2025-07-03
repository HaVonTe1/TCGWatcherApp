package de.dkutzer.tcgwatcher.collectables.history.domain

interface SearchCacheRepository {

    // Search related operations
    suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchWithBasicProductsInfo?
    suspend fun findSearchWithItemsAndSellOffersByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchWithFullProductInfo?
    suspend fun findSearchWithProductsNamesAndSetsByQuery(searchTerm: String, page: Int = 1, limit: Int = 5): SearchWithFullProductInfo?
    suspend fun persistSearchWithBasicProductsInfo(searchWithBasicProductsInfo: SearchWithBasicProductsInfo, language: String): SearchWithBasicProductsInfo
    suspend fun persistSearchWithProductAndSellOffers(searchWithProducts: SearchWithFullProductInfo, language: String) : SearchWithFullProductInfo
    suspend fun persistSearch(search: SearchEntity)
    suspend fun deleteSearch(search: SearchEntity)
    suspend fun getSearchHistory(): List<String>

    suspend fun removeProductsFromSearch(search: SearchEntity, products: List<ProductEntity>)

    // Product related operations
    suspend fun findProductWithSellOffersByExternalId(externalId: String) : ProductWithSellOffers?
    suspend fun getProductsByExternalId(externalId: String) : ProductWithSellOffers?
    suspend fun findProductsByLink(link: String) : List<ProductEntity>
    suspend fun persistProducts(results: List<ProductEntity>)
    suspend fun updateProductByDetailsUrl(
        detailsUrl: String,
        itemEntity: ProductEntity,
        names: List<ProductNameEntity> = emptyList(),
        sets: List<ProductSetEntity> = emptyList()
    )
    suspend fun updateProduct(productWithSellOffers: ProductWithSellOffers)
    suspend fun deleteProducts(results: List<ProductEntity>)

}