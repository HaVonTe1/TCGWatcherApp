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
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.RemoteKeyEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SellOfferEntity

@Dao
interface SearchCacheDao {


    @Query("SELECT id FROM search WHERE LOWER(searchTerm) = LOWER(:searchTerm)")
    fun findSearchIdBySearchTerm(searchTerm: String) : Int?

    @Query("SELECT * FROM search WHERE LOWER(searchTerm) = LOWER(:searchTerm)")
    fun findSearch(searchTerm: String) : SearchEntity?

    @Query("SELECT * FROM search_result_item WHERE searchId = :searchId LIMIT :pageSize OFFSET :offset")
    fun findSearchResultsBySearchId(searchId: Int, pageSize: Int, offset: Int): List<ProductEntity>

    @Transaction
    @Query("SELECT sri.* FROM search_result_item sri left join search s on s.id = sri.searchId WHERE LOWER(s.searchTerm) = LOWER(:searchTerm)")
    fun findItemsByQuery(searchTerm: String): PagingSource<Int, ProductEntity>

    //TODO: refactor result type to single entity after refactoring of N:M relation between search and item is done
    @Query("SELECT * FROM search_result_item WHERE externalId = :externalId")
    fun findProductsByExternalId(externalId: String): List<ProductWithSellOffers>

    @Transaction
    @Query("SELECT sri.* FROM search_result_item sri left join search s on s.id = sri.searchId WHERE LOWER(s.searchTerm) = LOWER(:searchTerm)")
    fun findItemsWithSellOffersByQuery(searchTerm: String): PagingSource<Int, ProductWithSellOffers>

    @Query("SELECT sri.* FROM search_result_item sri WHERE sri.externalId = :productId") //TODO: test
    fun findItemWithSellOffersByProductId(productId: String) : ProductWithSellOffers?

    @Query("SELECT searchTerm FROM search WHERE history = 1 ORDER BY lastUpdated DESC")
    fun getSearchHistory() : List<String>

    @Upsert
    fun saveItems(results: List<ProductEntity>): List<Long>

    @Upsert
    fun saveItem(item: ProductEntity): Long

    @Upsert
    fun saveSellOffers(offers: List<SellOfferEntity>): List<Long>

    @Query("SELECT * FROM product_offer WHERE productId = :productId")
    fun findSellOfferByProductId(productId: Int): List<SellOfferEntity>

    @Upsert
    fun saveSearch(search: SearchEntity) : Long

    @Query("UPDATE search SET lastUpdated = :lastUpdated WHERE id = :searchId")
    fun updateLastUpdated(searchId: Int, lastUpdated: Long)

    @Update
    fun updateSearch(search: SearchEntity)

    @Delete
    fun removeSearch(search: SearchEntity)

    @Delete
    fun removeItems(results: List<ProductEntity>)

    @Query("SELECT * FROM search_result_item WHERE externalLink = :link")
    fun findItemsByLink(link: String) : List<ProductEntity>

    @Query("UPDATE search_result_item SET " +
            "price = :price, " +
            "priceTrend = :priceTrend, " +
            "setName = :setName, " +
            "setId = :setLink, " +
            "rarity = :rarity, " +
            "type = :type, " +
            "lastUpdated = :lastUpdated WHERE " +
            "externalLink = :detailsUrl"
    )
    fun updateItemsByLink(detailsUrl: String, price: String, priceTrend: String,setName: String,setLink:String,rarity:String,type:String, lastUpdated: Long)


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