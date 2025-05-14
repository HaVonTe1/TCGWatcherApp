package de.dkutzer.tcgwatcher.collectables.search.data

import kotlin.test.Test
import kotlin.test.assertEquals

class BaseCardmarketApiClientKtTest {

    @Test
    fun `parseLink of breadcrump`()
    {
        val typePath = "/de/Pokemon/Products/Singles/Prismatic-Evolutions"
        val triple = parseLink(typePath)
        assertEquals("de", triple.first)
        assertEquals("Pokemon", triple.second)
        assertEquals("Singles", triple.third)
    }

    @Test
    fun `parseLink of setlink`()
    {
        val typePath = " /de/YuGiOh/Expansions/Alliance-Insight"
        val triple = parseLink(typePath)
        assertEquals("de", triple.first)
        assertEquals("YuGiOh", triple.second)
        assertEquals("Alliance-Insight", triple.third)

    }

}