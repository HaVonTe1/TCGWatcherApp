package de.dkutzer.tcgwatcher.collectables.history.data

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SearchCacheRepositoryImplTest {
    private lateinit var database: SearchCacheDatabase
    private lateinit var dao: SearchCacheDao
    private lateinit var repository: SearchCacheRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Initialize Robolectric
        RuntimeEnvironment.application = context.applicationContext as Application

        database = Room.inMemoryDatabaseBuilder(context, SearchCacheDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.searchCacheDao
        repository = SearchCacheRepositoryImpl(dao)
    }
}