package de.dkutzer.tcgwatcher.products.domain.port

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.dkutzer.tcgwatcher.products.domain.Converter
import de.dkutzer.tcgwatcher.products.domain.SearchEntity
import de.dkutzer.tcgwatcher.products.domain.SearchResultItemEntity
import de.dkutzer.tcgwatcher.products.domain.SearchWithResultsEntity

@Database(
    entities = [SearchEntity::class, SearchResultItemEntity::class],
    version = 1,
    exportSchema = false

)
@TypeConverters(Converter::class)
abstract class SearchCacheDatabase : RoomDatabase() {

    abstract val searchCacheDaoDa: SearchCacheDao

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