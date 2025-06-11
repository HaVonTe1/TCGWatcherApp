package de.dkutzer.tcgwatcher.collectables.history.domain

interface SearchCacheRepository {

    suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchAndProductsEntity?
    suspend fun findSearchWithItemsAndSellOffersByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchAndProductsAndSelloffersEntity?
    suspend fun findProductWithSellOffersByExternalId(externalId: String) : Product?
    suspend fun persistsSearchWithItems(searchWithProducts: SearchAndProductsEntity, language: String): SearchAndProductsEntity
    suspend fun persistSearchWithProductAndSellOffers(searchWithProducts: SearchAndProductsAndSelloffersEntity, language: String) : SearchAndProductsAndSelloffersEntity

    suspend fun getProductsByExternalId(externalId: String) : List<Product>

    suspend fun persistSearchItems(results: List<ProductItemEntity>)
    suspend fun getSearchHistory(): List<String>
    suspend fun deleteSearch(search: SearchEntity)
    suspend fun persistSearch(search: SearchEntity)
    suspend fun deleteSearchItems(results: List<ProductItemEntity>)
    suspend fun findItemsByLink(link: String) : List<ProductItemEntity>
    suspend fun updateItemByLink(detailsUrl: String, itemEntity: ProductItemEntity)
    suspend fun updateProduct(product: Product)
}