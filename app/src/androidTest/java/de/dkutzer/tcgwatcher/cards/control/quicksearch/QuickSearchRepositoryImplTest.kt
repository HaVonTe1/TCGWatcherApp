package de.dkutzer.tcgwatcher.cards.control.quicksearch

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class QuickSearchRepositoryImplTest {


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: QuickSearchDatabase
    private lateinit var dao: QuickSearchDao
    private lateinit var repository: QuickSearchRepository

    @Before
    fun setup() {
        database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            QuickSearchDatabase::class.java, "test_quicksearch_database")


            .allowMainThreadQueries()
            .createFromAsset("quicksearch.db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // 3
                    db.execSQL("INSERT INTO qs_pokemon_cards_fts(qs_pokemon_cards_fts) VALUES ('rebuild')")
                }
            })
            .build()
        dao = database.quicksearchDao

        repository = QuickSearchRepositoryImpl(dao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testMatching1() = runBlocking {
        // When
        val query = "evoli tg11"
        val results = repository.find(query)

        // Then

        results.forEach {
            println(it.nameDe)
        }
        Assert.assertEquals(1,results.size)

    }
}