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

    @Transaction
    @Query("SELECT * FROM search WHERE searchTerm = :searchTerm LIMIT :pageSize OFFSET :offset" )
    fun findBySearchTerm(searchTerm: String, pageSize: Int, offset: Int): SearchWithResultsEntity?

//    @Upsert(entity = SearchEntity::class)
//    fun persistResults(search: SearchWithResultsEntity)
//

}