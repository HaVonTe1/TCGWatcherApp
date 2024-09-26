package de.dkutzer.tcgwatcher.cards.control.cache

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import de.dkutzer.tcgwatcher.cards.entity.ProductItemEntity
import de.dkutzer.tcgwatcher.cards.entity.RemoteKeyEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchEntity

@Dao
interface SearchCacheDao {


    @Query("SELECT * FROM search WHERE LOWER(searchTerm) = LOWER(:searchTerm)")
    fun findSearch(searchTerm: String) : SearchEntity?

    @Query("SELECT * FROM search_result_item WHERE searchId = :searchId LIMIT :pageSize OFFSET :offset")
    fun findSearchResultsBySearchId(searchId: Int, pageSize: Int, offset: Int): List<ProductItemEntity>

    @Query("SELECT sri.* FROM search_result_item sri left join search s on s.searchId = sri.searchId WHERE LOWER(s.searchTerm) = LOWER(:searchTerm)")
    fun findItemsByQuery(searchTerm: String): PagingSource<Int, ProductItemEntity>

    @Query("SELECT searchTerm FROM search WHERE history = 1 ORDER BY lastUpdated DESC")
    fun getSearchHistory() : List<String>

    @Upsert
    fun persistItems(results: List<ProductItemEntity>): List<Long>

    @Upsert
    fun persistSearch( search: SearchEntity) : Long

    @Delete
    fun removeSearch(search: SearchEntity)

    @Delete
    fun removeItems(results: List<ProductItemEntity>)

    @Query("SELECT * FROM search_result_item WHERE cmLink = :link")
    fun findItemsByLink(link: String) : List<ProductItemEntity>

    @Query("UPDATE search_result_item SET price = :price, priceTrend = :priceTrend, orgName = :orgName, lastUpdated = :lastUpdated WHERE cmLink = :detailsUrl")
    fun updateItemsByLink(detailsUrl: String, price: String, priceTrend: String, orgName: String, lastUpdated: Long)


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