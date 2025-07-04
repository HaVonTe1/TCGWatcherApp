package de.dkutzer.tcgwatcher.collectables.history.domain

interface SearchCacheRepository {

    // Search related operations
    suspend fun getSearchWithBasicProductsByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchWithBasicProductsInfo?
    suspend fun getSearchWithFullProductsByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchWithFullProductInfo?
    suspend fun getFullProductInfoByExternalId(externalId: String) : ProductWithSellOffers?

    suspend fun persistSearchWithBasicProductsInfo(searchWithProducts: SearchWithProducts, language: String): SearchWithBasicProductsInfo
    suspend fun persistSearchWithProductAndSellOffers(searchWithProducts: SearchWithFullProductInfo, language: String) : SearchWithFullProductInfo
    suspend fun persistSearch(search: SearchEntity)
    suspend fun deleteSearch(search: SearchEntity)
    suspend fun getSearchHistory(): List<String>

    suspend fun removeProductsFromSearch(search: SearchEntity)

    // Product related operations
    suspend fun getProductsByExternalId(externalId: String) : ProductWithSellOffers?
    suspend fun persistProducts(results: List<ProductEntity>)
    suspend fun updateProductByDetailsUrl(
        detailsUrl: String,
        productEntity: ProductEntity,
        names: List<ProductNameEntity> = emptyList(),
        sets: List<ProductSetEntity> = emptyList()
    )
    suspend fun updateProduct(productWithSellOffers: ProductWithSellOffers)
    suspend fun deleteProducts(results: List<ProductEntity>)

}