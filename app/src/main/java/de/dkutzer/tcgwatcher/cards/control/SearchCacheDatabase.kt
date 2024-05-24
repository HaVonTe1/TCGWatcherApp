package de.dkutzer.tcgwatcher.cards.control

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.dkutzer.tcgwatcher.cards.entity.RemoteKeyEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemEntity

@Database(
    entities = [SearchEntity::class, SearchResultItemEntity::class,  RemoteKeyEntity::class],
    version = 1,
    exportSchema = false

)
@TypeConverters(Converter::class)
abstract class SearchCacheDatabase : RoomDatabase() {

    abstract val searchCacheDaoDa: SearchCacheDao
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