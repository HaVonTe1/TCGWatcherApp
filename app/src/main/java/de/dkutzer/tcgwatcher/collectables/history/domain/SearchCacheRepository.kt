package de.dkutzer.tcgwatcher.collectables.history.domain

interface SearchCacheRepository {

    suspend fun findSearchWithItemsByQuery(searchTerm: String, page: Int = 1, limit : Int = 5) : SearchAndProductsEntity?
    suspend fun persistsSearchWithItems(results: SearchAndProductsEntity, language: String): SearchAndProductsEntity
    suspend fun persistSearchItems(results: List<ProductItemEntity>)
    suspend fun getSearchHistory(): List<String>
    suspend fun deleteSearch(search: SearchEntity)
    suspend fun persistSearch(search: SearchEntity)
    suspend fun deleteSearchItems(results: List<ProductItemEntity>)
    suspend fun findItemsByLink(link: String) : List<ProductItemEntity>
    suspend fun updateItemByLink(detailsUrl: String, itemEntity: ProductItemEntity)
}