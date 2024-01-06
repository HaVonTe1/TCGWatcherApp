package de.dkutzer.tcgwatcher.products.domain

import androidx.room.*
import java.time.OffsetDateTime

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val language: Languages,
    val engine: Engines

)

@Entity(tableName = "search")
data class SearchEntity(
    @PrimaryKey(autoGenerate = true)
    val searchId: Int = 0,
    @ColumnInfo(index = true)
    val searchTerm: String,
    val size: Int,
    val lastUpdated: Long

)

@Entity(tableName = "search_result_item")
data class SearchResultItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val searchId: Int,
    val displayName: String,
    val orgName: String,
    val cmLink: String,
    val imgLink: String,
    val price: String
)


data class SearchWithResultsEntity(
    @Embedded val search: SearchEntity,
    @Relation(
        parentColumn = "searchId",
        entityColumn = "searchId"
    )
    val results: List<SearchResultItemEntity>
)

