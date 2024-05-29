package de.dkutzer.tcgwatcher.cards.control.quicksearch

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

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
                    db.execSQL("INSERT INTO qs_fts_pokemon_cards_fts(qs_fts_pokemon_cards_fts) VALUES ('rebuild')")
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
    fun testMatchingWithCode() = runBlocking {
        // When
        val query = "evoli tg11"
        val duration = measureTimeMillis {
            val results = repository.find(query)
           // Assert.assertEquals(1,results.size)
            println(results)

        }
        logger.info { "Time with $query: $duration"   }

        // Then


    }

    @Test
    fun testMatching2() = runBlocking {
        // When
        val query = "evoli"
        val duration = measureTimeMillis {
            val results = repository.find(query)
            //Assert.assertEquals(1,results.size)
            println(results)

        }
        logger.info { "Time with $query: $duration"   }

        // Then


    }


    @Test
    fun testMatching3() = runBlocking {
        // When
        val query = "evo"
        val duration = measureTimeMillis {
            val results = repository.find(query)
            //Assert.assertEquals(1,results.size)
            println(results)

        }
        logger.info { "Time with $query: $duration"   }


    // Then
    }

}