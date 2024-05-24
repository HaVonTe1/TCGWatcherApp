package de.dkutzer.tcgwatcher.cards.control

import androidx.paging.PagingSource
import androidx.room.*
import de.dkutzer.tcgwatcher.cards.entity.RemoteKeyEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemEntity

@Dao
interface SearchCacheDao {


    @Query("SELECT * FROM search WHERE searchTerm = :searchTerm")
    fun findSearch(searchTerm: String) : SearchEntity?

    @Query("SELECT * FROM search_result_item WHERE searchId = :searchId LIMIT :pageSize OFFSET :offset")
    fun findSearchResultsBySearchId(searchId: Int, pageSize: Int, offset: Int): List<SearchResultItemEntity>

    @Query("SELECT sri.* FROM search_result_item sri left join search s on s.searchId = sri.searchId WHERE s.searchTerm = :searchTerm")
    fun pagingSource(searchTerm: String): PagingSource<Int, SearchResultItemEntity>

    @Query("SELECT searchTerm FROM search ORDER BY lastUpdated DESC")
    fun getSearchHistory() : List<String>

    @Upsert()
    fun persistResults( results: List<SearchResultItemEntity>): List<Long>

    @Upsert
    fun persistSearch( search: SearchEntity) : Long


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