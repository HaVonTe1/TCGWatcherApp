package de.dkutzer.tcgwatcher.products.domain.port

import androidx.paging.PagingSource
import androidx.room.*
import de.dkutzer.tcgwatcher.products.domain.RemoteKeyEntity
import de.dkutzer.tcgwatcher.products.domain.SearchEntity
import de.dkutzer.tcgwatcher.products.domain.SearchResultItemEntity
import de.dkutzer.tcgwatcher.products.domain.SearchWithResultsEntity

@Dao
interface SearchCacheDao {


    @Query("SELECT * FROM search WHERE searchTerm = :searchTerm")
    fun findSearch(searchTerm: String) : SearchEntity?

    @Query("SELECT * FROM search_result_item WHERE searchId = :searchId LIMIT :pageSize OFFSET :offset")
    fun findSearchResultsBySearchId(searchId: Int, pageSize: Int, offset: Int): List<SearchResultItemEntity>

    @Query("SELECT sri.* FROM search_result_item sri left join search s on s.searchId = sri.searchId WHERE s.searchTerm = :searchTerm")
    fun pagingSource(searchTerm: String): PagingSource<Int, SearchResultItemEntity>

    @Query("DELETE FROM search_result_item")
    suspend fun clearResults()

    @Query("DELETE FROM search")
    suspend fun clearSearch()

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