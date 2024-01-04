package de.dkutzer.tcgwatcher.products.domain.port

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.dkutzer.tcgwatcher.products.domain.SearchResultItemEntity
import de.dkutzer.tcgwatcher.products.domain.SearchWithResultsEntity

@Dao
interface SearchCacheDao {

    @Transaction
    @Query("SELECT * FROM search WHERE searchTerm = :searchTerm")
    fun findBySearchTerm(searchTerm: String): SearchWithResultsEntity?

    @Upsert
    fun persistResults(search: SearchWithResultsEntity)


}