package de.dkutzer.tcgwatcher.cards.control.quicksearch

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class QuicksearchDaoTest
{

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: QuicksearchDatabase
    private lateinit var dao: QuicksearchDao

    @Before
    fun setup() {
        database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            QuicksearchDatabase::class.java, "test_quicksearch_database")


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
    }

//    @After
//    fun teardown() {
//        database.close()
//    }

    @Test
    fun testMatching1() = runBlocking {
        // When
        val query = "Pikachu"
        val results = dao.search(query)

        // Then
        assertEquals(1,results.size)

    }
}