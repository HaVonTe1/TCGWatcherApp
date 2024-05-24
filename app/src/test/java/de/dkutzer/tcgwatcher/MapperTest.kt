package de.dkutzer.tcgwatcher



import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemDto
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemEntity
import de.dkutzer.tcgwatcher.cards.control.toSearchItemEntity
import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

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
            price = "$10"
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

    // Converting a null SearchResultItemDto to SearchResultItemEntity should throw an exception
    @Test
    fun test_converting_null_dto_to_entity_throws_exception() {
        // Given
        val searchId = 1L
        val dto: SearchResultItemDto? = null

        // When

        assertFailsWith(
            exceptionClass = IllegalArgumentException::class,
            block = {
                dto?.toSearchItemEntity(searchId)
            }
        )
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
            price = "$20"
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

    @Test
    fun test_creating_entity_with_invalid_parameters_throws_exception() {
        // Given
        val searchId = 1L
        val dto = SearchResultItemDto(
            displayName = "Item 1",
            orgName = "Org 1",
            cmLink = "https://example.com/item1",
            imgLink = "https://example.com/item1.jpg",
            price = "$10"
        )

        // When
        assertFailsWith(
            exceptionClass = IllegalArgumentException::class,
            block = {
                SearchResultItemEntity(
                    id = -1,
                    searchId = searchId.toInt(),
                    displayName = dto.displayName,
                    orgName = dto.orgName,
                    cmLink = dto.cmLink,
                    imgLink = dto.imgLink,
                    price = dto.price
                )
            }
        )
        assertFailsWith(
            exceptionClass = IllegalArgumentException::class,
            block = {
                SearchResultItemEntity(
                    id = 1,
                    searchId = -1,
                    displayName = dto.displayName,
                    orgName = dto.orgName,
                    cmLink = dto.cmLink,
                    imgLink = dto.imgLink,
                    price = dto.price
                )
            }
        )

    }
}


