package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.history.data.SearchCacheRepositoryImplTest
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductComposite
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductNameEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductSetEntity
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.history.domain.SellOfferEntity
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
import de.dkutzer.tcgwatcher.collectables.search.domain.SellOfferModel
import de.dkutzer.tcgwatcher.collectables.search.domain.SetDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SetModel
import de.dkutzer.tcgwatcher.collectables.search.domain.SpecialType
import de.dkutzer.tcgwatcher.collectables.search.domain.TypeEnum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

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
    fun `toProductModel from ProductComposite with multiple names`() {
        // Arrange
        val composite = createSampleProductComposite()

        // Act
        val result = composite.toProductModel()

        // Assert
        assertEquals(2, result.names.size)
        assertEquals("Test Product", result.names[0].value)
        assertEquals("en", result.names[0].languageCode)
        assertEquals("Test Produkt", result.names[1].value)
        assertEquals("de", result.names[1].languageCode)
        assertEquals("123", result.id)
    }

    @Test
    fun `toProductModel from ProductAggregate with sell offers`() {
        // Arrange
        val aggregate = createSampleProductAggregate()

        // Act
        val result = aggregate.toProductModel("en")

        // Assert
        assertEquals(1, result.sellOffers.size)
        assertEquals("Test Seller", result.sellOffers[0].sellerName)
        assertEquals("Germany", result.sellOffers[0].sellerLocation.country)
        assertEquals("de", result.sellOffers[0].sellerLocation.code)
    }

    @Test
    fun `toProductWithSellofferEntity from ProductModel creates complete aggregate`() {
        // Arrange
        val productModel = createSampleProductModel()

        // Act
        val result = productModel.toProductWithSellofferEntity( productId = 10)

        // Assert
        assertEquals(10, result.productEntity.id)
        assertEquals(1, result.names.size)
        assertEquals("Test Product", result.names.first().name)
        assertEquals("en", result.names[0].language)
        assertEquals(10, result.names[0].productId)
        assertEquals("Test Set", result.set?.setName)
        assertEquals("http://example.com/set", result.set?.setId)
    }

    @Test
    fun `toSellOfferEntity from SellOfferModel maps correctly`() {
        // Arrange
        val sellOfferModel = createSampleSellOfferModel()

        // Act
        val result = sellOfferModel.toSellOfferEntity(productId = 42)

        // Assert
        assertEquals(42, result.productId)
        assertEquals("Test Seller", result.sellerName)
        assertEquals("de", result.sellerLocation)
        assertEquals("en", result.productLanguage)
        assertEquals("Near Mint", result.condition)
        assertEquals(3, result.amount)
        assertEquals("25.50", result.price)
    }

    @Test
    fun `toSellOfferModel from SellOfferEntity maps correctly`() {
        // Arrange
        val sellOfferEntity = createSampleSellOfferEntity()

        // Act
        val result = sellOfferEntity.toSellOfferModel("de")

        // Assert
        assertEquals(sellOfferEntity.sellerName, result.sellerName)
        assertEquals("Deutschland", result.sellerLocation.country)
        assertEquals("de", result.sellerLocation.code)
        assertEquals("Deutsch", result.productLanguage.displayName)
        assertEquals("de", result.productLanguage.code)
        assertEquals(ConditionType.NEAR_MINT, result.condition)
        assertEquals(1, result.amount)
        assertEquals("15.00", result.price)
    }

    @Test
    fun `toProductGallaryItemDto from CardmarketProductDetailsDto`() {
        // Arrange
        val detailsDto = createSampleProductDetailsDto("en")

        // Act
        val result = detailsDto.toProductGallaryItemDto()

        // Assert
        assertEquals(detailsDto.name, result.name)
        assertEquals(detailsDto.code, result.code)
        assertEquals(detailsDto.detailsUrl, result.cmLink)
        assertEquals(detailsDto.cmId, result.cmId)
        assertEquals(detailsDto.imageUrl, result.imgLink)
        assertEquals(detailsDto.price, result.price)
        assertEquals(detailsDto.genre, result.genre)
        assertEquals(detailsDto.type, result.type)
        assertEquals(detailsDto.priceTrend, result.priceTrend)
    }

    @Test
    fun `toProduct from CardmarketProductDetailsDto creates complete aggregate`() {
        // Arrange
        val detailsDto = createSampleProductDetailsDto("de")

        // Act
        val result = detailsDto.toProductWithSellOffersEntity("de", productId = 5)

        // Assert
        assertEquals(5, result.productEntity.id)
        assertEquals(1, result.offers.size)
        assertEquals("Seller 1", result.offers[0].sellerName)
        assertEquals(1, result.names.size)
        assertEquals("Test Produkt", result.names[0].name)
        assertEquals("de", result.names[0].language)
        assertEquals("Test Set", result.set?.setName)
        assertEquals("/de/Pokemon/Products/Singles/Team-Rocket/", result.set?.setId)
    }

    @Test
    fun `toModel from NameDto`() {
        // Arrange
        val nameDto = NameDto("Test Name", "fr", "Test Original")

        // Act
        val result = nameDto.toModel()

        // Assert
        assertEquals("Test Name", result.value)
        assertEquals("fr", result.languageCode)
    }

    @Test
    fun `fromString with valid enum value`() {
        // Act
        val result = fromString<TypeEnum>("Singles")

        // Assert
        assertEquals(TypeEnum.CARD, result)
    }

    @Test
    fun `fromString with invalid enum value returns OTHER`() {
        // Act
        val result = fromString<GenreType>("InvalidGenre")

        // Assert
        assertEquals(GenreType.OTHER, result)
    }

    @Test
    fun `fromString with case insensitive matching`() {
        // Act
        val result = fromString<RarityType>("RARE")

        // Assert
        assertEquals(RarityType.RARE, result)
    }

    @Test
    fun `test LocationModel fromSellerLocation`() {
        var locationModel = LocationModel.fromSellerLocation("Deutschland", "de")
        assertEquals("Deutschland", locationModel.country)
        assertEquals("de", locationModel.code)


        locationModel = LocationModel.fromSellerLocation("Germany", "en")
        assertEquals("Germany", locationModel.country)
        assertEquals("de", locationModel.code)

        locationModel = LocationModel.fromSellerLocation("Italien", "de")
        assertEquals("Italien", locationModel.country)
        assertEquals("it", locationModel.code)


        locationModel = LocationModel.fromSellerLocation("Italy", "en")
        assertEquals("Italy", locationModel.country)
        assertEquals("it", locationModel.code)



    }

    @Test
    fun `test LanguageModel fromProductLanguage`() {
        run {
            val fromProductLanguage = LanguageModel.fromProductLanguage("Deutsch", "de")
            assertEquals("de", fromProductLanguage.code)
            assertEquals("Deutsch", fromProductLanguage.displayName)
        }
        run {
            val fromProductLanguage = LanguageModel.fromProductLanguage("Englisch", "de")
            assertEquals("en", fromProductLanguage.code)
            assertEquals("Englisch", fromProductLanguage.displayName)
        }
        run {
            val fromProductLanguage = LanguageModel.fromProductLanguage("Französisch", "de")
            assertEquals("fr", fromProductLanguage.code)
            assertEquals("Französisch", fromProductLanguage.displayName)
        }
        run {
            val fromProductLanguage = LanguageModel.fromProductLanguage("Koreanisch", "de")
            assertEquals("ko", fromProductLanguage.code)
            assertEquals("Koreanisch", fromProductLanguage.displayName)
        }
        run {
            val fromProductLanguage = LanguageModel.fromProductLanguage("German", "en")
            assertEquals("de", fromProductLanguage.code)
            assertEquals("German", fromProductLanguage.displayName)
        }
        run {
            val fromProductLanguage = LanguageModel.fromProductLanguage("English", "en")
            assertEquals("en", fromProductLanguage.code)
            assertEquals("English", fromProductLanguage.displayName)
        }
        run {
            val fromProductLanguage = LanguageModel.fromProductLanguage("French", "en")
            assertEquals("fr", fromProductLanguage.code)
            assertEquals("French", fromProductLanguage.displayName)
        }
        run {
            val fromProductLanguage = LanguageModel.fromProductLanguage("Korean", "en")
            assertEquals("ko", fromProductLanguage.code)
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
        val productModel = productDetailsDto.toProductModel("de")
        assertEquals("Test Produkt", productModel.names[0].value)
        assertEquals("de", productModel.names[0].languageCode)
        assertEquals("TEST123", productModel.code)
        assertEquals("Pokemon", productModel.genre.cmCode)
        assertEquals("Pokemon", productModel.genre.displayName)
        assertEquals("Singles", productModel.type.cmCode)
        assertEquals("Card", productModel.type.displayName)
        assertEquals("Test Set", productModel.set.name)
        assertEquals("https://product-images.s3.cardmarket.com/51/TR/274089/274089.jpg", productModel.imageUrl)
        assertEquals("/de/Pokemon/Products/Singles/Team-Rocket/Dark-Gloom-TR36", productModel.detailsUrl)
        assertEquals("/Pokemon/Products/Singles/Team-Rocket/Dark-Gloom-TR36", productModel.externalId)
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
        val productModel = productDetailsDto.toProductModel("en")
        assertEquals("Test Produkt", productModel.names[0].value)
        assertEquals("en", productModel.names[0].languageCode)
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

    @Test
    fun `should create product entity with valid input`() {
        // Arrange
        val dto = CardmarketProductGallaryItemDto(
            name = NameDto("xx", "de", "yy"),
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
        val entity = dto.toProductItemEntity()

        // Assert
        assertEquals(dto.cmLink, entity.externalLink)
        assertEquals(dto.imgLink, entity.imgLink)
        assertEquals(dto.price, entity.price)
        assertEquals(dto.cmId, entity.externalId)
        assertEquals(dto.name.languageCode, entity.language)
        assertEquals(dto.genre, entity.genre)
        assertEquals(dto.type, entity.type)
    }

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
            cmId = "/Pokemon/Products/Singles/Team-Rocket/Dark-Gloom-TR36",
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
        sellerLocation = "Deutschland",
        productLanguage = "Deutsch",
        special = "Reverse Holo",
        condition = "Near Mint",
        amount = "10",
        price = "50.00"
    )

    private fun createSampleSellOfferDtoEn(): CardmarketSellOfferDto = CardmarketSellOfferDto(
        sellerName = "Seller 1",
        sellerLocation = "Germany",
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
            cmId = "Dark-Gloom-TR36",
            imgLink = "https://product-images.s3.cardmarket.com/51/TR/274089/274089.jpg",
            price = "100.00",
            priceTrend = priceTrend
        )
    }

    private fun createSampleProductComposite(): ProductComposite {
        return ProductComposite(
            productEntity = ProductEntity(
                id = 123,
                language = "en",
                genre = "Pokemon",
                type = "Singles",
                rarity = "Rare",
                code = "TEST123",
                externalId = "test-id",
                externalLink = "/en/Pokemon/Products/Singles/test/123",
                imgLink = "http://example.com/image.jpg",
                price = "25.00",
                priceTrend = "+10%",
                lastUpdated = Instant.now().epochSecond
            ),
            names = listOf(
                ProductNameEntity(id = 1, productId = 123, name = "Test Product", language = "en"),
                ProductNameEntity(id = 2, productId = 123, name = "Test Produkt", language = "de")
            ),
            set = ProductSetEntity(
                id = 1,
                productId = 123,
                setName = "Test Set",
                setId = "test-set",
                language = "en"
            )
        )
    }

    private fun createSampleProductAggregate(): ProductWithSellOffers {
        return ProductWithSellOffers(
            productEntity = ProductEntity(
                id = 456,
                language = "en",
                genre = "Pokemon",
                type = "Singles",
                rarity = "Rare",
                code = "AGG123",
                externalId = "agg-id",
                externalLink = "/en/Pokemon/Products/Singles/agg/456",
                imgLink = "http://example.com/agg.jpg",
                price = "30.00",
                priceTrend = "+5%",
                lastUpdated = Instant.now().epochSecond
            ),
            offers = listOf(
                SellOfferEntity(
                    id = 1,
                    productId = 456,
                    sellerName = "Test Seller",
                    sellerLocation = "de",
                    productLanguage = "en",
                    condition = "Near Mint",
                    amount = 2,
                    price = "28.00",
                    special = ""
                )
            ),
            names = listOf(
                ProductNameEntity(
                    id = 3,
                    productId = 456,
                    name = "Aggregate Product",
                    language = "en"
                )
            ),
            set = ProductSetEntity(
                id = 2,
                productId = 456,
                setName = "Aggregate Set",
                setId = "agg-set",
                language = "en"
            )
        )
    }

    private fun createSampleProductModel(): ProductModel {
        return ProductModel(
            id = "model-123",
            names = listOf(NameModel("Test Product", "en")),
            code = "MODEL123",
            type = TypeEnum.CARD,
            genre = GenreType.POKEMON,
            rarity = RarityType.RARE,
            set = SetModel("http://example.com/set", "Test Set"),
            detailsUrl = "/en/Pokemon/Products/Singles/model/123",
            externalId = "model-id",
            imageUrl = "http://example.com/model.jpg",
            price = "35.00",
            priceTrend = "+15%",
            sellOffers = emptyList(),
            timestamp = Instant.now().epochSecond
        )
    }

    private fun createSampleSellOfferModel(): SellOfferModel {
        return SellOfferModel(
            sellerName = "Test Seller",
            sellerLocation = LocationModel(country = "Germany", code = "de"),
            productLanguage = LanguageModel(code = "en", displayName = "English"),
            special = SpecialType.OTHER,
            condition = ConditionType.NEAR_MINT,
            amount = 3,
            price = "25.50"
        )
    }

    private fun createSampleSellOfferEntity(): SellOfferEntity {
        return SearchCacheRepositoryImplTest.TestHelpers.createSampleSellOfferEntity(price = "15.00", productId = 789)
    }

    private fun assertCommonGalleryDtoMapping(
        dto: CardmarketProductGallaryItemDto,
        entity: ProductEntity
    ) {
        assertEquals(dto.genre, entity.genre)
        assertEquals(dto.type, entity.type)
        assertEquals(dto.cmLink, entity.externalLink)
        assertEquals(dto.imgLink, entity.imgLink)
        assertEquals(dto.price, entity.price)
        assertEquals(dto.name.languageCode, entity.language)
    }
}