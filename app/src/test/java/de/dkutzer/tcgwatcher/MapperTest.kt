package de.dkutzer.tcgwatcher



import de.dkutzer.tcgwatcher.cards.control.toSearchItemEntity
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemDto
import junit.framework.TestCase.assertEquals
import org.junit.Test

class MapperTest {


    // Should create a SearchResultItemEntity object with the correct displayName, orgName, cmLink, imgLink, price and searchId when called with valid input
    @Test
    fun should_create_search_result_item_entity_with_valid_input() {
        // Arrange
        val searchId = 1L
        val dto = SearchResultItemDto(
            displayName = "Item 1",
            orgName = "Org 1",
            cmLink = "https://example.com/item1",
            imgLink = "https://example.com/item1.jpg",
            price = "$10",
            priceTrend = "sdf"
        )

        // Act
        val entity = dto.toSearchItemEntity(searchId)

        // Assert
        assertEquals(dto.displayName, entity.displayName)
        assertEquals(dto.orgName, entity.orgName)
        assertEquals(dto.cmLink, entity.cmLink)
        assertEquals(dto.imgLink, entity.imgLink)
        assertEquals(dto.price, entity.price)
        assertEquals(searchId.toInt(), entity.searchId)
    }


    // Should create a SearchResultItemEntity object with the correct displayName, orgName, cmLink, imgLink, price and searchId when called with valid input and a non-zero searchId
    @Test
    fun should_create_search_result_item_entity_with_valid_input_and_non_zero_search_id() {
        // Given
        val searchId = 10L
        val dto = SearchResultItemDto(
            displayName = "Item 2",
            orgName = "Org 2",
            cmLink = "https://example.com/item2",
            imgLink = "https://example.com/item2.jpg",
            price = "$20",
            priceTrend = "sf"
        )

        // When
        val entity = dto.toSearchItemEntity(searchId)

        // Then
        assertEquals(dto.displayName, entity.displayName)
        assertEquals(dto.orgName, entity.orgName)
        assertEquals(dto.cmLink, entity.cmLink)
        assertEquals(dto.imgLink, entity.imgLink)
        assertEquals(dto.price, entity.price)
        assertEquals(searchId.toInt(), entity.searchId)
    }


}


