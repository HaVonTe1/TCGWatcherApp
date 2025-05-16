package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductGallaryItemDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CodeType
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType
import de.dkutzer.tcgwatcher.collectables.search.domain.LocationModel
import de.dkutzer.tcgwatcher.collectables.search.domain.NameDto
import de.dkutzer.tcgwatcher.collectables.search.domain.NameModel
import de.dkutzer.tcgwatcher.collectables.search.domain.PriceTrendType
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.RarityType
import de.dkutzer.tcgwatcher.collectables.search.domain.SetModel
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import kotlin.math.abs

class MapperTest {

    @Test
    fun `toProductItemEntity from CardmarketProductGallaryItemDto with valid code`() {
        // Arrange
        val validCode = CodeType("PRE 004", true)
        val dto = createSampleGalleryItemDto(code = validCode)

        // Act
        val result = dto.toProductItemEntity()

        // Assert
        assertEquals(validCode.value, result.code)
        assertCommonGalleryDtoMapping(dto, result)
    }

    @Test
    fun `toProductItemEntity from CardmarketProductGallaryItemDto with invalid code`() {
        // Arrange
        val invalidCode = CodeType("INVALID", false)
        val dto = createSampleGalleryItemDto(code = invalidCode)

        // Act
        val result = dto.toProductItemEntity()

        // Assert
        assertEquals("", result.code)
        assertCommonGalleryDtoMapping(dto, result)
    }

    @Test
    fun `toProductItemEntity from CardmarketProductGallaryItemDto with valid priceTrend`() {
        // Arrange
        val validTrend = PriceTrendType("+10%", true)
        val dto = createSampleGalleryItemDto(priceTrend = validTrend)

        // Act
        val result = dto.toProductItemEntity()

        // Assert
        assertEquals(validTrend.value, result.priceTrend)
        assertCommonGalleryDtoMapping(dto, result)
    }

    @Test
    fun `toProductItemEntity from CardmarketProductGallaryItemDto with invalid priceTrend`() {
        // Arrange
        val invalidTrend = PriceTrendType("N/A", false)
        val dto = createSampleGalleryItemDto(priceTrend = invalidTrend)

        // Act
        val result = dto.toProductItemEntity()

        // Assert
        assertEquals("", result.priceTrend)
        assertCommonGalleryDtoMapping(dto, result)
    }

    @Test
    fun `toProductItemEntity from CardmarketProductGallaryItemDto with default searchId`() {
        // Arrange
        val dto = createSampleGalleryItemDto()

        // Act
        val result = dto.toProductItemEntity()

        // Assert
        assertEquals(0, result.searchId)
    }

    @Test
    fun `toProductItemEntity from CardmarketProductGallaryItemDto with specific searchId`() {
        // Arrange
        val dto = createSampleGalleryItemDto()
        val expectedSearchId = 5

        // Act
        val result = dto.toProductItemEntity(searchId = expectedSearchId.toLong())

        // Assert
        assertEquals(expectedSearchId, result.searchId)
    }

    @Test
    fun `toProductItemEntity from CardmarketProductGallaryItemDto timestamp generation`() {
        // Arrange
        val dto = createSampleGalleryItemDto()
        val beforeTime = Instant.now().epochSecond

        // Act
        val result = dto.toProductItemEntity()
        val afterTime = Instant.now().epochSecond

        // Assert
        assertTrue(result.lastUpdated in beforeTime..afterTime)
    }

    @Test
    fun `toProductModel from ProductItemEntity with valid cmLink`() {
        // Arrange
        val cmLink = "/de/Pokemon/Products/Singles/set123/card456"
        val entity = createSampleProductItemEntity(cmLink = cmLink)

        // Act
        val result = entity.toProductModel()

        // Assert
        assertEquals("card456", result.id)
        assertCommonProductModelMapping(entity, result)
    }

    @Test
    fun `toProductModel from ProductItemEntity with various TypeEnum strings`() {
        // Valid types
        val validMappings = listOf(
            "Singles" to TypeEnum.CARD,
            "Boosters" to TypeEnum.BOOSTER,
            "Booster-Boxes" to TypeEnum.DISPLAY,
            "Theme-Decks" to TypeEnum.THEME_DECK,
            "Trainer-Kits" to TypeEnum.TRAINER_KIT,
            "Tins" to TypeEnum.TIN,
            "Box-Sets" to TypeEnum.BOX_SET,
            "Elite-Trainer-Boxes" to TypeEnum.ELITE_TRAINER_BOX,
            "Blisters" to TypeEnum.BLISTER
        )

        validMappings.forEach { (typeStr, expectedType) ->
            val entity = createSampleProductItemEntity(type = typeStr)
            val result = entity.toProductModel()
            assertEquals(expectedType, result.type)
        }

        // Invalid type
        val invalidEntity = createSampleProductItemEntity(type = "InvalidType")
        assertEquals(TypeEnum.OTHER, invalidEntity.toProductModel().type)
    }

    @Test
    fun `toProductModel from ProductItemEntity with various GenreType strings`() {
        val validMappings = listOf(
            "Pokemon" to GenreType.POKEMON,
            "Magic" to GenreType.MAGIC,
            "YuGiOh" to GenreType.YUGIOH
        )

        validMappings.forEach { (genreStr, expectedGenre) ->
            val entity = createSampleProductItemEntity(genre = genreStr)
            val result = entity.toProductModel()
            assertEquals(expectedGenre, result.genre)
        }

        // Invalid genre
        val invalidEntity = createSampleProductItemEntity(genre = "UnknownGenre")
        assertEquals(GenreType.OTHER, invalidEntity.toProductModel().genre)
    }

    @Test
    fun `toProductModel from ProductItemEntity with various RarityType strings`() {
        val validMappings = listOf(
            "Common" to RarityType.COMMON,
            "Uncommon" to RarityType.UNCOMMON,
            "Rare" to RarityType.RARE,
            "Double Rare" to RarityType.DOUBLE_RARE,
            "Secret Rare" to RarityType.SECRET_RARE,
            "Illustration Rare" to RarityType.ILLUSTRATION_RARE,
            "Special Illustration Rare" to RarityType.SPECIAL_ILLUSTRATION_RARE,
            "Promo" to RarityType.PROMO,
            "Fixed" to RarityType.FIXED,
            "Ultra Rare" to RarityType.ULTRA_RARE
        )

        validMappings.forEach { (rarityStr, expectedRarity) ->
            val entity = createSampleProductItemEntity(rarity = rarityStr)
            val result = entity.toProductModel()
            assertEquals(expectedRarity, result.rarity)
        }

        // Invalid rarity
        val invalidEntity = createSampleProductItemEntity(rarity = "UnknownRarity")
        assertEquals(RarityType.OTHER, invalidEntity.toProductModel().rarity)
    }

    @Test
    fun `toProductItemEntity from ProductModel with default searchId`() {
        // Arrange
        val model = createSampleProductModel()

        // Act
        val result = model.toProductItemEntity()

        // Assert
        assertEquals(0, result.searchId)
    }

    @Test
    fun `toProductItemEntity from ProductModel with specific searchId`() {
        // Arrange
        val model = createSampleProductModel()
        val expectedSearchId = 10

        // Act
        val result = model.toProductItemEntity(searchId = expectedSearchId)

        // Assert
        assertEquals(expectedSearchId, result.searchId)
    }

    @Test
    fun `toProductItemEntity from ProductModel basic mapping`() {
        // Arrange
        val model = createSampleProductModel()

        // Act
        val result = model.toProductItemEntity()

        // Assert
        assertEquals(model.name.value, result.displayName)
        assertEquals(model.type.cmCode, result.type)
        assertEquals(model.genre.cmCode, result.genre)
        assertEquals(model.rarity.cmCode, result.rarity)
        assertEquals(model.code, result.code)
        assertEquals(model.detailsUrl, result.cmLink)
        assertEquals(model.imageUrl, result.imgLink)
        assertEquals(model.price, result.price)
        assertEquals(model.priceTrend, result.priceTrend)
        assertEquals(model.set.name, result.setName)
        assertEquals(model.set.link, result.setLink)
    }

    @Test
    fun `test LocationModel fromSellerLocation`() {


        var locationModel = LocationModel.fromSellerLocation("Artikelstandort: Deutschland", "de")
        assertEquals("Deutschland", locationModel.country)
        assertEquals("de", locationModel.code)


        locationModel = LocationModel.fromSellerLocation("Item location: Germany", "en")
        assertEquals("Germany", locationModel.country)
        assertEquals("de", locationModel.code)

        locationModel = LocationModel.fromSellerLocation("Artikelstandort: Italien", "de")
        assertEquals("Italien", locationModel.country)
        assertEquals("it", locationModel.code)


        locationModel = LocationModel.fromSellerLocation("Item location: Italy", "en")
        assertEquals("Italy", locationModel.country)
        assertEquals("it", locationModel.code)
    }

    // Helper functions to create sample data
    private fun createSampleGalleryItemDto(
        code: CodeType = CodeType("TEST123", true),
        priceTrend: PriceTrendType = PriceTrendType("+5%", true)
    ): CardmarketProductGallaryItemDto {
        return CardmarketProductGallaryItemDto(
            name = NameDto("Test Product", "en"),
            code = code,
            genre = "Pokemon",
            type = "Singles",
            cmLink = "/de/Pokemon/Products/Singles/Team-Rocket/Dark-Gloom-TR36",
            imgLink = "https://product-images.s3.cardmarket.com/51/TR/274089/274089.jpg",
            price = "100.00",
            priceTrend = priceTrend
        )
    }

    private fun createSampleProductItemEntity(
        cmLink: String = "/de/Pokemon/Products/Singles/set123/card456",
        type: String = "Singles",
        genre: String = "Pokemon",
        rarity: String = "Rare"
    ): ProductItemEntity {
        return ProductItemEntity(
            searchId = 0,
            displayName = "Test Product",
            language = "en",
            genre = genre,
            type = type,
            rarity = rarity,
            code = "TEST123",
            orgName = "Org Name",
            cmLink = cmLink,
            imgLink = "http://example.com/image.jpg",
            price = "100.00",
            priceTrend = "+5%",
            setName = "Test Set",
            setLink = "http://example.com/set",
            lastUpdated = Instant.now().epochSecond
        )
    }

    private fun createSampleProductModel(): ProductModel {
        return ProductModel(
            id = "Lillies-Clefairy-ex-V1-JTG056",
            name = NameModel("Test Product", "en", "Test Product"),
            type = TypeEnum.CARD,
            genre = GenreType.POKEMON,
            code = "TEST123",
            imageUrl = "http://example.com/image.jpg",
            detailsUrl = "/en/Pokemon/Products/Singles/set123/card456",
            rarity = RarityType.RARE,
            set = SetModel("http://example.com/set", "Test Set"),
            price = "100.00",
            priceTrend = "+5%",
            sellOffers = emptyList(),
            timestamp = Instant.now().epochSecond
        )
    }

    private fun assertCommonGalleryDtoMapping(
        dto: CardmarketProductGallaryItemDto,
        entity: ProductItemEntity
    ) {
        assertEquals(dto.name.value, entity.displayName)
        assertEquals(dto.genre, entity.genre)
        assertEquals(dto.type, entity.type)
        assertEquals(dto.cmLink, entity.cmLink)
        assertEquals(dto.imgLink, entity.imgLink)
        assertEquals(dto.price, entity.price)
    }

    private fun assertCommonProductModelMapping(
        entity: ProductItemEntity,
        model: ProductModel
    ) {
        assertEquals(entity.displayName, model.name.value)
        assertEquals(entity.genre, model.genre.cmCode)
        assertEquals(entity.type, model.type.cmCode)
        assertEquals(entity.rarity, model.rarity.cmCode)
        assertEquals(entity.code, model.code)
        assertEquals(entity.cmLink, model.detailsUrl)
        assertEquals(entity.imgLink, model.imageUrl)
        assertEquals(entity.price, model.price)
        assertEquals(entity.priceTrend, model.priceTrend)
        assertEquals(entity.setName, model.set.name)
        assertEquals(entity.setLink, model.set.link)
    }

    // Additional helper assertions for timestamps
    private fun assertTimestampRecent(timestamp: Long) {
        val current = Instant.now().epochSecond
        assertTrue(abs(current - timestamp) <= 2)
    }
}