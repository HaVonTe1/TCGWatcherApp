package de.dkutzer.tcgwatcher.collectables.history.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductNameEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductSetEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.RemoteKeyEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchProductCrossRef
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithBasicProductsInfo
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithFullProductInfo
import de.dkutzer.tcgwatcher.collectables.history.domain.SellOfferEntity

@Dao
interface SearchCacheDao {

    @Query(
        "SELECT DISTINCT p.* " +
                "FROM search_result_item AS p " +
                "INNER JOIN search_product_cross_ref AS ref ON p.id = ref.productId " +
                "INNER JOIN search AS s ON s.id = ref.searchId " +
                "WHERE s.id = :searchId ORDER BY p.id ASC LIMIT :pageSize OFFSET :offset"
    )
    fun getProductsBySearchId(searchId: Int, pageSize: Int, offset: Int): List<ProductEntity>


    @Query("SELECT id FROM search WHERE LOWER(searchTerm) = LOWER(:searchTerm)")
    fun getSearchIdBySearchTerm(searchTerm: String) : Int?

    @Query("SELECT * FROM search WHERE LOWER(searchTerm) = LOWER(:searchTerm)")
    fun getSearchByTerm(searchTerm: String) : SearchEntity?

    @Transaction
    @Query(
        "SELECT DISTINCT p.* " +
                "FROM search_result_item AS p " +
                "INNER JOIN search_product_cross_ref AS ref ON p.id = ref.productId " +
                "INNER JOIN search AS s ON s.id = ref.searchId " +
                "WHERE LOWER(s.searchTerm) = LOWER(:searchTerm)"
    )
    fun getProductsBySearchTerm(searchTerm: String): PagingSource<Int, ProductEntity>


    @Transaction
    @Query("SELECT * FROM search_result_item WHERE externalId = :externalId")
    fun getProductWithSellOffersByExternalId(externalId: String): ProductWithSellOffers?

    @Transaction
    @Query(
        "SELECT DISTINCT p.* " +
                "FROM search_result_item AS p " +
                "INNER JOIN search_product_cross_ref AS ref ON p.id = ref.productId " +
                "INNER JOIN search AS s ON s.id = ref.searchId " +
                "WHERE LOWER(s.searchTerm) = LOWER(:searchTerm)"
    )
    fun getProductWithSellOffersPagingSource(searchTerm: String): PagingSource<Int, ProductWithSellOffers>

    @Transaction
    @Query("SELECT sri.* FROM search_result_item sri WHERE sri.externalId = :productId") //TODO: test
    fun getProductWithSellOffersByProductId(productId: String) : ProductWithSellOffers?

    @Query("SELECT searchTerm FROM search WHERE history = 1 ORDER BY lastUpdated DESC")
    fun getSearchHistory() : List<String>

    @Upsert
    fun upsertProducts(results: List<ProductEntity>): List<Long>

    @Upsert
    fun upsertProduct(item: ProductEntity): Long

    @Upsert
    fun upsertSellOffers(offers: List<SellOfferEntity>): List<Long>

    @Query("SELECT * FROM product_offer WHERE productId = :productId")
    fun getSellOffersByProductId(productId: Int): List<SellOfferEntity>

    @Upsert()
    fun upsertSearch(search: SearchEntity) : Long

    @Query("UPDATE search SET lastUpdated = :lastUpdated WHERE id = :searchId")
    fun updateLastUpdated(searchId: Int, lastUpdated: Long)

    @Update
    fun updateSearch(search: SearchEntity)

    @Delete
    fun deleteSearch(search: SearchEntity)

    @Delete
    fun deleteProducts(results: List<ProductEntity>)

    @Query("SELECT * FROM search_result_item WHERE externalLink = :link")
    fun getProductsByLink(link: String) : List<ProductEntity>

    // --- ProductNameEntity ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProductNames(names: List<ProductNameEntity>): List<Long>

    @Query("SELECT * FROM product_name WHERE productId = :productId")
    fun getProductNames(productId: Int): List<ProductNameEntity>

    @Delete
    fun deleteProductNames(names: List<ProductNameEntity>)

    @Upsert()
    fun upsertProductSets(sets: List<ProductSetEntity>): List<Long>

    @Query("SELECT * FROM product_set WHERE productId = :productId")
    fun getProductSets(productId: Int): List<ProductSetEntity>

    @Delete
    fun deleteProductSets(sets: List<ProductSetEntity>)

    // CrossRef-Operationen für M:N
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearchProductCrossRefs(crossRefs: List<SearchProductCrossRef>): List<Long>

    @Query("DELETE FROM search_product_cross_ref WHERE searchId = :searchId")
    fun deleteCrossRefsBySearchId(searchId: Int)

    @Query("DELETE FROM search_product_cross_ref WHERE productId = :productId")
    fun deleteCrossRefsByProductId(productId: Int)

    @Transaction
    @Query("SELECT * FROM search as s " +
            "JOIN search_product_cross_ref as spcr ON s.id = spcr.searchId " +
            "JOIN search_result_item  as p ON spcr.productId = p.id " +
            "WHERE LOWER(s.searchTerm) = LOWER(:searchTerm) ORDER BY p.id ASC LIMIT :limit OFFSET :offset"
    )
    fun getSearchWithProducts(searchTerm: String, limit: Int, offset: Int): SearchWithBasicProductsInfo?

    @Transaction
    @Query("SELECT * FROM search as s " +
            "JOIN search_product_cross_ref as spcr ON s.id = spcr.searchId " +
            "JOIN search_result_item  as p ON spcr.productId = p.id " +
            "WHERE LOWER(s.searchTerm) = LOWER(:searchTerm) " +
            "ORDER BY p.id ASC LIMIT :limit OFFSET :offset"
    )
    fun getSearchWithProductsAndSellOffers(searchTerm: String, limit: Int, offset: Int): SearchWithFullProductInfo?
}

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RemoteKeyEntity)

    @Query("SELECT * FROM remote_key WHERE id = :id")
    suspend fun getById(id: String): RemoteKeyEntity?

    @Query("DELETE FROM remote_key WHERE id = :id")
    suspend fun deleteById(id: String)
}