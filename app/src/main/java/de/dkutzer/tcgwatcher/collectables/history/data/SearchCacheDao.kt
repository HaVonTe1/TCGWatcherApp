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
import de.dkutzer.tcgwatcher.collectables.history.domain.Product
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.RemoteKeyEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SellOfferEntity

@Dao
interface SearchCacheDao {


    @Query("SELECT id FROM search WHERE LOWER(searchTerm) = LOWER(:searchTerm)")
    fun getSearchIdBySearchTerm(searchTerm: String) : Int?

    @Query("SELECT * FROM search WHERE LOWER(searchTerm) = LOWER(:searchTerm)")
    fun findSearch(searchTerm: String) : SearchEntity?

    @Query("SELECT * FROM search_result_item WHERE searchId = :searchId LIMIT :pageSize OFFSET :offset")
    fun findSearchResultsBySearchId(searchId: Int, pageSize: Int, offset: Int): List<ProductItemEntity>

    @Transaction
    @Query("SELECT sri.* FROM search_result_item sri left join search s on s.id = sri.searchId WHERE LOWER(s.searchTerm) = LOWER(:searchTerm)")
    fun findItemsByQuery(searchTerm: String): PagingSource<Int, ProductItemEntity>

    @Transaction
    @Query("SELECT sri.* FROM search_result_item sri left join search s on s.id = sri.searchId WHERE LOWER(s.searchTerm) = LOWER(:searchTerm)")
    fun findItemsWithSellOffersByQuery(searchTerm: String): PagingSource<Int, Product>

    @Query("SELECT sri.* FROM search_result_item sri WHERE sri.orgName = :productId") //TODO: test
    fun findItemWithSellOffersByProductId(productId: String) : Product?

    @Query("SELECT searchTerm FROM search WHERE history = 1 ORDER BY lastUpdated DESC")
    fun getSearchHistory() : List<String>

    @Upsert
    fun persistItems(results: List<ProductItemEntity>): List<Long>

    @Upsert
    fun persistItem(item: ProductItemEntity): Long

    @Upsert
    fun persistSellOffers(offers: List<SellOfferEntity>): List<Long>

    @Query("SELECT * FROM product_offer WHERE productId = :productId")
    fun findSellOfferByProductId(productId: Int): List<SellOfferEntity>

    @Upsert
    fun persistSearch( search: SearchEntity) : Long

    @Query("UPDATE search SET lastUpdated = :lastUpdated WHERE id = :searchId")
    fun updateLastUpdated(searchId: Int, lastUpdated: Long)

    @Update
    fun updateSearch(search: SearchEntity)

    @Delete
    fun removeSearch(search: SearchEntity)

    @Delete
    fun removeItems(results: List<ProductItemEntity>)

    @Query("SELECT * FROM search_result_item WHERE cmLink = :link")
    fun findItemsByLink(link: String) : List<ProductItemEntity>

    @Query("UPDATE search_result_item SET " +
            "price = :price, " +
            "priceTrend = :priceTrend, " +
            "orgName = :orgName, " +
            "setName = :setName, " +
            "setLink = :setLink, " +
            "rarity = :rarity, " +
            "type = :type, " +
            "lastUpdated = :lastUpdated WHERE " +
            "cmLink = :detailsUrl")
    fun updateItemsByLink(detailsUrl: String, price: String, priceTrend: String, orgName: String,setName: String,setLink:String,rarity:String,type:String, lastUpdated: Long)


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