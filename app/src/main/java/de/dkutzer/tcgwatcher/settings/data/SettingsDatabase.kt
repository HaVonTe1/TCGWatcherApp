package de.dkutzer.tcgwatcher.settings.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [SettingsEntity::class],
    version = 1,
    exportSchema = false

)
@TypeConverters(Converter::class)
abstract class SettingsDatabase : RoomDatabase() {

    abstract val settingsDao: SettingsDao

    //Highlander Pattern
    companion object {
        @Volatile
        private var Instance: SettingsDatabase? = null

        fun getDatabase(context: Context): SettingsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, SettingsDatabase::class.java, "settings_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }

}