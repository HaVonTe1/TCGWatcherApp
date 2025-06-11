package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.history.domain.ProductItemEntity
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductGallaryItemDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketSellOfferDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CodeType
import de.dkutzer.tcgwatcher.collectables.search.domain.ConditionType
import de.dkutzer.tcgwatcher.collectables.search.domain.GenreType
import de.dkutzer.tcgwatcher.collectables.search.domain.LanguageModel
import de.dkutzer.tcgwatcher.collectables.search.domain.LocationModel
import de.dkutzer.tcgwatcher.collectables.search.domain.NameDto
import de.dkutzer.tcgwatcher.collectables.search.domain.NameModel
import de.dkutzer.tcgwatcher.collectables.search.domain.PriceTrendType
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.collectables.search.domain.RarityType
import de.dkutzer.tcgwatcher.collectables.search.domain.SetDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SetModel
import de.dkutzer.tcgwatcher.collectables.search.domain.SpecialType
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
            "Coins" to TypeEnum.COIN,
            "Lots" to TypeEnum.LOT,
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
        assertEquals(model.set.link, result.setId)
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

    @Test
    fun `test LanguageModel fromProductLanguage`() {

        {
            val fromProductLanguage = LanguageModel.fromProductLanguage("Deutsch", "de")
            assertEquals("de", fromProductLanguage.code)
            assertEquals("Deutsch", fromProductLanguage.displayName)

        }
        {
            val fromProductLanguage = LanguageModel.fromProductLanguage("Englisch", "de")
            assertEquals("en", fromProductLanguage.code)
            assertEquals("Englisch", fromProductLanguage.displayName)

        }
        {
            val fromProductLanguage = LanguageModel.fromProductLanguage("Französisch", "de")
            assertEquals("fr", fromProductLanguage.code)
            assertEquals("Französisch", fromProductLanguage.displayName)

        }
        {
            val fromProductLanguage = LanguageModel.fromProductLanguage("Koreanisch", "de")
            assertEquals("kr", fromProductLanguage.code)
            assertEquals("Koreanisch", fromProductLanguage.displayName)

        }
        {
            val fromProductLanguage = LanguageModel.fromProductLanguage("German", "en")
            assertEquals("de", fromProductLanguage.code)
            assertEquals("German", fromProductLanguage.displayName)

        }
        {
            val fromProductLanguage = LanguageModel.fromProductLanguage("English", "en")
            assertEquals("en", fromProductLanguage.code)
            assertEquals("English", fromProductLanguage.displayName)

        }
        {
            val fromProductLanguage = LanguageModel.fromProductLanguage("French", "en")
            assertEquals("fr", fromProductLanguage.code)
            assertEquals("French", fromProductLanguage.displayName)

        }
        {
            val fromProductLanguage = LanguageModel.fromProductLanguage("Korean", "en")
            assertEquals("kr", fromProductLanguage.code)
            assertEquals("Korean", fromProductLanguage.displayName)

        }
    }

    @Test
    fun `test LocationModel fromCode`() {

        val locationModel = LocationModel.fromCode("de", "de")
        assertEquals("Deutschland", locationModel.country)
        assertEquals("de", locationModel.code)

        val locationModel2 = LocationModel.fromCode("it", "en")
        assertEquals("Italy", locationModel2.country)
        assertEquals("it", locationModel2.code)

        val locationModel3 = LocationModel.fromCode("fr", "en")
        assertEquals("France", locationModel3.country)
        assertEquals("fr", locationModel3.code)

    }

    @Test
    fun `test LanguageModel fromCode`() {
        val languageModel = LanguageModel.fromCode("de", "de")
        assertEquals("de", languageModel.code)
        assertEquals("Deutsch", languageModel.displayName)
        val languageModel2 = LanguageModel.fromCode("it", "en")
        assertEquals("it", languageModel2.code)
        assertEquals("Italian", languageModel2.displayName)

        val languageModel3 = LanguageModel.fromCode("fr", "en")
        assertEquals("fr", languageModel3.code)
        assertEquals("French", languageModel3.displayName)
    }

    @Test
    fun `test ProductDetailsDto ToProductModel de`() {

        val productDetailsDto = createSampleProductDetailsDto("de")
        val productModel = productDetailsDto.toProductModel()
        assertEquals("Test Produkt", productModel.name.value)
        assertEquals("de", productModel.name.languageCode)
        assertEquals("Test Product", productModel.name.i18n)
        assertEquals("TEST123", productModel.code)
        assertEquals("Pokemon", productModel.genre.cmCode)
        assertEquals("Pokemon", productModel.genre.displayName)
        assertEquals("Singles", productModel.type.cmCode)
        assertEquals("Card", productModel.type.displayName)
        assertEquals("Test Set", productModel.set.name)
        assertEquals("https://product-images.s3.cardmarket.com/51/TR/274089/274089.jpg", productModel.imageUrl)
        assertEquals("/de/Pokemon/Products/Singles/Team-Rocket/Dark-Gloom-TR36", productModel.detailsUrl)
        assertEquals("Rare", productModel.rarity.cmCode)
        assertEquals("Rare", productModel.rarity.displayName)
        assertEquals("100.00", productModel.price)
        assertEquals("+5%", productModel.priceTrend)
        assertEquals(1, productModel.sellOffers.size)
        assertEquals("Seller 1", productModel.sellOffers[0].sellerName)
        assertEquals("Deutschland", productModel.sellOffers[0].sellerLocation.country)
        assertEquals("de", productModel.sellOffers[0].sellerLocation.code)
        assertEquals("Deutsch", productModel.sellOffers[0].productLanguage.displayName)
        assertEquals("de", productModel.sellOffers[0].productLanguage.code)
        assertEquals(SpecialType.REVERSED, productModel.sellOffers[0].special)
        assertEquals(ConditionType.NEAR_MINT, productModel.sellOffers[0].condition)
        assertEquals(10, productModel.sellOffers[0].amount)
        assertEquals("50.00", productModel.sellOffers[0].price)
    }


    @Test
    fun `test ProductDetailsDto ToProductModel en`() {

        val productDetailsDto = createSampleProductDetailsDto("en")
        val productModel = productDetailsDto.toProductModel()
        assertEquals("Test Produkt", productModel.name.value)
        assertEquals("en", productModel.name.languageCode)
        assertEquals("Test Product", productModel.name.i18n)
        assertEquals("TEST123", productModel.code)
        assertEquals("Pokemon", productModel.genre.cmCode)
        assertEquals("Pokemon", productModel.genre.displayName)
        assertEquals("Singles", productModel.type.cmCode)
        assertEquals("Card", productModel.type.displayName)
        assertEquals("Test Set", productModel.set.name)
        assertEquals("https://product-images.s3.cardmarket.com/51/TR/274089/274089.jpg", productModel.imageUrl)
        assertEquals("/en/Pokemon/Products/Singles/Team-Rocket/Dark-Gloom-TR36", productModel.detailsUrl)
        assertEquals("Rare", productModel.rarity.cmCode)
        assertEquals("Rare", productModel.rarity.displayName)
        assertEquals("100.00", productModel.price)
        assertEquals("+5%", productModel.priceTrend)
        assertEquals(1, productModel.sellOffers.size)
        assertEquals("Seller 1", productModel.sellOffers[0].sellerName)
        assertEquals("Germany", productModel.sellOffers[0].sellerLocation.country)
        assertEquals("de", productModel.sellOffers[0].sellerLocation.code)
        assertEquals("German", productModel.sellOffers[0].productLanguage.displayName)
        assertEquals("de", productModel.sellOffers[0].productLanguage.code)
        assertEquals(SpecialType.REVERSED, productModel.sellOffers[0].special)
        assertEquals(ConditionType.NEAR_MINT, productModel.sellOffers[0].condition)
        assertEquals(10, productModel.sellOffers[0].amount)
        assertEquals("50.00", productModel.sellOffers[0].price)
    }

    //------------------------------------

    // Helper functions to create sample data

    private fun createSampleProductDetailsDto(lang: String): CardmarketProductDetailsDto {
        return CardmarketProductDetailsDto(
            name = NameDto("Test Produkt", lang,"Test Product"),
            code = CodeType("TEST123", true),
            genre = "Pokemon",
            type = "Singles",
            rarity = "Rare",
            detailsUrl = "/$lang/Pokemon/Products/Singles/Team-Rocket/Dark-Gloom-TR36",
            imageUrl = "https://product-images.s3.cardmarket.com/51/TR/274089/274089.jpg",
            price = "100.00",
            priceTrend = PriceTrendType("+5%", true),
            set = SetDto("Test Set", "/$lang/Pokemon/Products/Singles/Team-Rocket/"),
            sellOffers = listOf(
                when(lang) {
                    "de" -> createSampleSellOfferDtoDe()
                    "en" -> createSampleSellOfferDtoEn()
                    else -> throw IllegalArgumentException("Unsupported language: $lang")
                }
            )
        )

    }

    private fun createSampleSellOfferDtoDe(): CardmarketSellOfferDto = CardmarketSellOfferDto(
        sellerName = "Seller 1",
        sellerLocation = "Artikelstandort: Deutschland",
        productLanguage = "Deutsch",
        special = "Reverse Holo",
        condition = "Near Mint",
        amount = "10",
        price = "50.00"

    )

    private fun createSampleSellOfferDtoEn(): CardmarketSellOfferDto = CardmarketSellOfferDto(
        sellerName = "Seller 1",
        sellerLocation = "Location: Germany",
        productLanguage = "German",
        special = "Reverse Holo",
        condition = "Near Mint",
        amount = "10",
        price = "50.00"

    )
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
            setId = "http://example.com/set",
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
        assertEquals(entity.setId, model.set.link)
    }

    // Additional helper assertions for timestamps
    private fun assertTimestampRecent(timestamp: Long) {
        val current = Instant.now().epochSecond
        assertTrue(abs(current - timestamp) <= 2)
    }
}