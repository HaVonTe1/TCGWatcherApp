package de.dkutzer.tcgwatcher.cards.control.quicksearch

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import de.dkutzer.tcgwatcher.cards.entity.PokemonCardQuickEntity
import de.dkutzer.tcgwatcher.cards.entity.PokemonCardQuickEntityFTS


@Database(
    entities = [PokemonCardQuickEntity::class, PokemonCardQuickEntityFTS::class],
    version = 1,
    exportSchema = false
)
abstract class QuicksearchDatabase : RoomDatabase() {

    abstract val quicksearchDao: QuicksearchDao

    //Highlander Pattern
    companion object {
        @Volatile
        private var Instance: QuicksearchDatabase? = null

        fun getDatabase(context: Context): QuicksearchDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, QuicksearchDatabase::class.java, "quicksearch_database")
                    .createFromAsset("quicksearch.db")
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 3
                            db.execSQL("INSERT INTO qs_pokemon_cards_fts(qs_pokemon_cards_fts) VALUES ('rebuild')")
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }

}