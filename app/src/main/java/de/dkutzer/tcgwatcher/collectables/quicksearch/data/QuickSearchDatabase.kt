package de.dkutzer.tcgwatcher.collectables.quicksearch.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import de.dkutzer.tcgwatcher.collectables.quicksearch.domain.PokemonCardQuickEntity
import de.dkutzer.tcgwatcher.collectables.quicksearch.domain.PokemonCardQuickEntityFTS
import de.dkutzer.tcgwatcher.collectables.quicksearch.domain.PokemonCardQuickNormalizedEntity


@Database(
    entities = [PokemonCardQuickEntity::class, PokemonCardQuickEntityFTS::class, PokemonCardQuickNormalizedEntity::class],
    version = 3,
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
                            db.execSQL("INSERT INTO qs_fts_pokemon_cards_fts(qs_fts_pokemon_cards_fts) VALUES ('rebuild')")
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }

}