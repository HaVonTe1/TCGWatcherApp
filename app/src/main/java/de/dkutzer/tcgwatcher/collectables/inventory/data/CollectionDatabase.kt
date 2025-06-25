package de.dkutzer.tcgwatcher.collectables.inventory.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CollectionEntity::class, CollectionEntryEntity::class],
    version = 1
)
abstract class CollectionDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
}
