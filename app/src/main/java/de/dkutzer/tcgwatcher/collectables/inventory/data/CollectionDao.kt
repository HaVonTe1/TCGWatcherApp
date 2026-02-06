package de.dkutzer.tcgwatcher.collectables.inventory.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    // Collections
    @Query("SELECT * FROM collections")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    // Collection Entries
    @Query("SELECT * FROM collection_entries WHERE collectionId = :collectionId")
    fun getEntriesForCollection(collectionId: Long): Flow<List<CollectionEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CollectionEntryEntity): Long

    @Delete
    suspend fun deleteEntry(entry: CollectionEntryEntity)
}
