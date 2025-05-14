package de.dkutzer.tcgwatcher.collectables.history.domain

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType
import java.time.Instant

class Entities {
}

@Entity(tableName = "search")
data class SearchEntity(
    @PrimaryKey(autoGenerate = true)
    val searchId: Int = 0,
    @ColumnInfo(index = true)
    val searchTerm: String,
    val size: Int,
    val lastUpdated: Long,
    val history: Boolean
)

@Entity(tableName = "search_result_item")
data class ProductItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(index = true)
    var searchId: Int,
    val displayName: String,
    val language: String = "en",
    val genre: String = "",
    val type: String = "",
    val rarity: String = "",
    val code: String,
    val orgName: String,
    val cmLink: String,
    val imgLink: String,
    val price: String,
    val priceTrend: String,
    val lastUpdated: Long
)

@Entity("remote_key")
data class RemoteKeyEntity(
    @PrimaryKey val id: String,
    val nextOffset: Int,
)

data class SearchWithItemsEntity(
    @Embedded val search: SearchEntity,
    @Relation(
        parentColumn = "searchId",
        entityColumn = "searchId"
    )
    val results: List<ProductItemEntity>

) {
    fun isOlderThan(seconds: Long): Boolean {

        return Instant.ofEpochSecond(this.search.lastUpdated).isBefore(Instant.now().minusSeconds(seconds))
    }
}

