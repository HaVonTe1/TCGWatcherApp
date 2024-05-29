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
abstract class QuickSearchDatabase : RoomDatabase() {

    abstract val quicksearchDao: QuickSearchDao

    //Highlander Pattern
    companion object {
        @Volatile
        private var Instance: QuickSearchDatabase? = null

        fun getDatabase(context: Context): QuickSearchDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, QuickSearchDatabase::class.java, "quicksearch_database")
                    .createFromAsset("quicksearch.db")
                    .addCallback(object : Callback() {
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