package de.dkutzer.tcgwatcher.products.domain.port

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.dkutzer.tcgwatcher.products.domain.SearchEntity
import de.dkutzer.tcgwatcher.products.domain.SearchResultItemEntity
import de.dkutzer.tcgwatcher.products.domain.SearchWithResultsEntity

@Dao
interface SearchCacheDao {


    @Query("SELECT * FROM search WHERE searchTerm = :searchTerm")
    fun findSearch(searchTerm: String) : SearchEntity?

    @Query("SELECT * FROM search_result_item WHERE searchId = :searchId LIMIT :pageSize OFFSET :offset")
    fun findSearchResultsBySearchId(searchId: Int, pageSize: Int, offset: Int): List<SearchResultItemEntity>

    @Upsert()
    fun persistResults( results: List<SearchResultItemEntity>): List<Long>

    @Upsert
    fun persistSearch( search: SearchEntity) : Long


}