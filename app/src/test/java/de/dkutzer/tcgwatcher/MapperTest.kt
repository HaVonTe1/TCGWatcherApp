package de.dkutzer.tcgwatcher



import de.dkutzer.tcgwatcher.collectables.search.data.toProductItemEntity
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductGallaryItemDto
import de.dkutzer.tcgwatcher.collectables.search.domain.NameDto
import junit.framework.TestCase.assertEquals
import org.junit.Test

class MapperTest {


    // Should create a SearchResultItemEntity object with the correct displayName, orgName, cmLink, imgLink, price and searchId when called with valid input
    @Test
    fun should_create_search_result_item_entity_with_valid_input() {
        // Arrange
        val searchId = 1L
        val dto = CardmarketProductGallaryItemDto(
            name = NameDto("xx","de","yy"),
            code = "TST 1",
            genre = "xx",
            type = "xx",
            cmLink = "https://example.com/item1",
            cmId = "item1",
            imgLink = "https://example.com/item1.jpg",
            price = "$10",
            priceTrend = "sdf"
        )

        // Act
        val entity = dto.toProductItemEntity(searchId)

        // Assert
        assertEquals(dto.name.value, entity.displayName)
        assertEquals(dto.name.i18n, entity.orgName)
        assertEquals(dto.cmLink, entity.externalLink)
        assertEquals(dto.imgLink, entity.imgLink)
        assertEquals(dto.price, entity.price)
        assertEquals(searchId.toInt(), entity.searchId)
    }


    // Should create a SearchResultItemEntity object with the correct displayName, orgName, cmLink, imgLink, price and searchId when called with valid input and a non-zero searchId
    @Test
    fun should_create_search_result_item_entity_with_valid_input_and_non_zero_search_id() {
        // Given
        val searchId = 10L
        val dto = CardmarketProductGallaryItemDto(
            name = NameDto("xx","de","yy"),
            code = "TST 1",
            genre = "xx",
            type = "xx",
            cmLink = "https://example.com/item1",
            cmId = "item1",
            imgLink = "https://example.com/item1.jpg",
            price = "$10",
            priceTrend = "sdf"
        )

        // When
        val entity = dto.toProductItemEntity(searchId)

        // Then
        assertEquals(dto.name.value, entity.displayName)
        assertEquals(dto.name.i18n, entity.orgName)
        assertEquals(dto.cmLink, entity.externalLink)
        assertEquals(dto.imgLink, entity.imgLink)
        assertEquals(dto.price, entity.price)
        assertEquals(searchId.toInt(), entity.searchId)
    }


}


