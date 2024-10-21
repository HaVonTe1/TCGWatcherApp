package de.dkutzer.tcgwatcher.collectables.control.quicksearch

import de.dkutzer.tcgwatcher.collectables.quicksearch.data.QuickSearchDao
import de.dkutzer.tcgwatcher.collectables.quicksearch.data.QuickSearchRepositoryImpl
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class QuickSearchRespositoryTest {

    @get:Rule
    val mockkRule = MockKRule(this)


    @Before
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @MockK
    lateinit var quickSearchDao: QuickSearchDao



    @Test
    fun `Testing an empty query should return an empty Wrapper`() {
        val quickSearchRepository =  QuickSearchRepositoryImpl(quickSearchDao)
        val sanatizeQuery = quickSearchRepository.sanatizeQuery("")
        assert(sanatizeQuery.isEmpty())
    }


    @Test
    fun `Testing wildcards for every element with single element`() {
        val quickSearchRepository =  QuickSearchRepositoryImpl(quickSearchDao)
        val sanatizeQuery = quickSearchRepository.sanatizeQuery("blaa")
        assert(!sanatizeQuery.isEmpty())
        assert(sanatizeQuery.names == "*blaa*")
        assert(sanatizeQuery.code.isEmpty())

    }

    @Test
    fun `Testing wildcards for every element with multiple elements`() {
        val quickSearchRepository =  QuickSearchRepositoryImpl(quickSearchDao)
        val sanatizeQuery = quickSearchRepository.sanatizeQuery("blaa blub")
        assert(!sanatizeQuery.isEmpty())
        assert(sanatizeQuery.names == "*blaa* *blub*")
        assert(sanatizeQuery.code.isEmpty())

    }

    @Test
    fun `Testing removal of all elements with less then 4 characters and no digits`() {
        val quickSearchRepository =  QuickSearchRepositoryImpl(quickSearchDao)
        val sanatizeQuery = quickSearchRepository.sanatizeQuery("bla blub")
        assert(!sanatizeQuery.isEmpty())
        assert(sanatizeQuery.names == "*blub*")
        assert(sanatizeQuery.code.isEmpty())
    }

    @Test
    fun `Testing removal of all elements with less then 4 characters and no digits with code`() {
        val quickSearchRepository =  QuickSearchRepositoryImpl(quickSearchDao)
        val sanatizeQuery = quickSearchRepository.sanatizeQuery("11 blub")
        assert(!sanatizeQuery.isEmpty())
        assert(sanatizeQuery.names == "*blub*")
        assert(sanatizeQuery.code == "%11%")
    }


    @Test
    fun `Testing removal of all elements with less then 4 characters and no digits with code reverse`() {
        val quickSearchRepository =  QuickSearchRepositoryImpl(quickSearchDao)
        val sanatizeQuery = quickSearchRepository.sanatizeQuery("blub 11")
        assert(!sanatizeQuery.isEmpty())
        assert(sanatizeQuery.names == "*blub*")
        assert(sanatizeQuery.code == "%11%")
    }

    @Test
    fun `Testing removal of all elements with less then 4 characters and no digits with mixed code `() {
        val quickSearchRepository =  QuickSearchRepositoryImpl(quickSearchDao)
        val sanatizeQuery = quickSearchRepository.sanatizeQuery("blub gt11")
        assert(!sanatizeQuery.isEmpty())
        assert(sanatizeQuery.names == "*blub*")
        assert(sanatizeQuery.code == "%gt11%")
    }


    @Test
    fun `Testing removal of all elements with less then 4 characters and no digits with uppercase `() {
        val quickSearchRepository =  QuickSearchRepositoryImpl(quickSearchDao)
        val sanatizeQuery = quickSearchRepository.sanatizeQuery("blUB GT11")
        assert(!sanatizeQuery.isEmpty())
        assert(sanatizeQuery.names == "*blub*")
        assert(sanatizeQuery.code == "%gt11%")
    }



    @Test
    fun `Testing multiple codes `() {
        val quickSearchRepository =  QuickSearchRepositoryImpl(quickSearchDao)
        val sanatizeQuery = quickSearchRepository.sanatizeQuery("blub gt11 22")
        assert(!sanatizeQuery.isEmpty())
        assert(sanatizeQuery.names == "*blub*")
        assert(sanatizeQuery.code == "%gt11%")
    }

    @Test
    fun `Testing multiple codes reversed `() {
        val quickSearchRepository =  QuickSearchRepositoryImpl(quickSearchDao)
        val sanatizeQuery = quickSearchRepository.sanatizeQuery("33 blub gt11")
        assert(!sanatizeQuery.isEmpty())
        assert(sanatizeQuery.names == "*blub* *gt11*")
        assert(sanatizeQuery.code == "%33%")
    }
}