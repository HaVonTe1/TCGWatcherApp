package de.dkutzer.tcgwatcher.collectables.control.quicksearch

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.dkutzer.tcgwatcher.collectables.quicksearch.data.QuickSearchDao
import de.dkutzer.tcgwatcher.collectables.quicksearch.data.QuickSearchDatabase
import de.dkutzer.tcgwatcher.collectables.quicksearch.data.QuickSearchRepositoryImpl
import de.dkutzer.tcgwatcher.collectables.quicksearch.domain.QuickSearchRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class QuickSearchRepositoryImplTest {

    private lateinit var database: QuickSearchDatabase
    private lateinit var dao: QuickSearchDao
    private lateinit var repository: QuickSearchRepository

    @Before
    fun setup() {

        val context = ApplicationProvider.getApplicationContext<Context>()
        // Initialize Robolectric
        RuntimeEnvironment.application = context.applicationContext as Application

        database = Room.databaseBuilder(
            context,
            QuickSearchDatabase::class.java, "test_quicksearch_db")
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