package de.dkutzer.tcgwatcher.collectables.history.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.dkutzer.tcgwatcher.collectables.history.domain.RemoteKeyEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity

@Database(
    entities = [SearchEntity::class, ProductItemEntity::class,  RemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SearchCacheDatabase : RoomDatabase() {

    abstract val searchCacheDao: SearchCacheDao
    abstract val remoteKeyDao: RemoteKeyDao

    //Highlander Pattern
    companion object {
        @Volatile
        private var Instance: SearchCacheDatabase? = null

        fun getDatabase(context: Context): SearchCacheDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, SearchCacheDatabase::class.java, "search_cache_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }

}