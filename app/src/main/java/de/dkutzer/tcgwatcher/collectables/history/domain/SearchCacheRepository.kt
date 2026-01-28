package de.dkutzer.tcgwatcher.collectables.history.domain

interface SearchCacheRepository {

    // Search related operations
    suspend fun getSearchWithBasicProductsByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchWithBasicProductsInfo?
    suspend fun getSearchWithFullProductsByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchWithFullProductInfo?
    suspend fun getFullProductInfoByExternalId(externalId: String) : ProductWithSellOffers?

    suspend fun persistSearchWithProducts(searchWithProducts: SearchWithProducts, language: String)
    suspend fun persistSearch(search: SearchEntity)
    suspend fun getSearchHistory(): List<String>

    suspend fun removeProductsFromSearch(search: SearchEntity)

    // Product related operations
    suspend fun getProductsByExternalId(externalId: String) : ProductWithSellOffers?
//    suspend fun persistProductByDetailsUrl(
//        detailsUrl: String,
//        productEntity: ProductEntity,
//        names: List<ProductNameEntity> = emptyList(),
//        set: ProductSetEntity?
//    )
    suspend fun persistProductWithSellOffers(productWithSellOffers: ProductWithSellOffers)

}