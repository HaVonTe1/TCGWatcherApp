package de.dkutzer.tcgwatcher.collectables.inventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null
)

@Entity(tableName = "collection_entries")
data class CollectionEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val collectionId: Long,
    val productId: String,
    val condition: String,
    val language: String,
    val price: Double?,
    val origin: String,
    val date: String // ISO-8601 Datum
)
