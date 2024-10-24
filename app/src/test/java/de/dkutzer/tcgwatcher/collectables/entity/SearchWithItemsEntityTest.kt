import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.SearchWithItemsEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SearchWithItemsEntityTest {

    private lateinit var searchEntity: SearchEntity
    private lateinit var productItemEntities: List<ProductItemEntity>
    private lateinit var searchWithItemsEntity: SearchWithItemsEntity

    @Before
    fun setUp() {
        // Mock the SearchEntity and ProductItemEntity
        searchEntity = mockk<SearchEntity>()
        productItemEntities = listOf(mockk<ProductItemEntity>())

        // Create an instance of SearchWithItemsEntity
        searchWithItemsEntity = SearchWithItemsEntity(searchEntity, productItemEntities)
    }

    @Test
    fun `isOlderThan should return false when search is not older than given seconds`() {
        // Mock the lastUpdated time to be now
        every { searchEntity.lastUpdated } returns Instant.now().epochSecond

        // Test with a small number of seconds (e.g., 1 second)
        assertFalse(searchWithItemsEntity.isOlderThan(1))
    }

    @Test
    fun `isOlderThan should return true when search is older than given seconds`() {
        // Mock the lastUpdated time to be 10 seconds ago
        every { searchEntity.lastUpdated } returns Instant.now().minusSeconds(10).epochSecond

        // Test with 5 seconds
        assertTrue(searchWithItemsEntity.isOlderThan(5))
    }

    @Test
    fun `isOlderThan should return true when search is exactly the given seconds old`() {
        // Mock the lastUpdated time to be exactly 5 seconds ago
        every { searchEntity.lastUpdated } returns Instant.now().minusSeconds(5).epochSecond

        // Test with 5 seconds
        assertTrue(searchWithItemsEntity.isOlderThan(5))
    }
}