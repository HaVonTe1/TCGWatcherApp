package de.dkutzer.tcgwatcher.collectables.inventory.domain

import de.dkutzer.tcgwatcher.collectables.inventory.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface CollectionRepository {
    fun getAllCollections(): Flow<List<CollectionModel>>
    suspend fun insertCollection(collection: CollectionModel): Long
    suspend fun deleteCollection(collection: CollectionModel)

    fun getEntriesForCollection(collectionId: Long): Flow<List<CollectionEntryModel>>
    suspend fun insertEntry(entry: CollectionEntryModel): Long
    suspend fun deleteEntry(entry: CollectionEntryModel)
}

class CollectionRepositoryImpl(
    private val dao: CollectionDao
) : CollectionRepository {
    override fun getAllCollections(): Flow<List<CollectionModel>> =
        dao.getAllCollections().map { list -> list.map { it.toDomain() } }

    override suspend fun insertCollection(collection: CollectionModel): Long =
        dao.insertCollection(collection.toEntity())

    override suspend fun deleteCollection(collection: CollectionModel) =
        dao.deleteCollection(collection.toEntity())

    override fun getEntriesForCollection(collectionId: Long): Flow<List<CollectionEntryModel>> =
        dao.getEntriesForCollection(collectionId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertEntry(entry: CollectionEntryModel): Long =
        dao.insertEntry(entry.toEntity())

    override suspend fun deleteEntry(entry: CollectionEntryModel) =
        dao.deleteEntry(entry.toEntity())
}
