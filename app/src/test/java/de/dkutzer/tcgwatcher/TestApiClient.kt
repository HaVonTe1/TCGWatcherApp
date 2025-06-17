package de.dkutzer.tcgwatcher

import de.dkutzer.tcgwatcher.collectables.search.data.cardmarket.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CodeType
import de.dkutzer.tcgwatcher.collectables.search.domain.NameDto
import de.dkutzer.tcgwatcher.collectables.search.domain.PriceTrendType
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SetDto

class TestApiClient : BaseCardmarketApiClient() {
    override suspend fun search(searchString: String, page: Int): SearchResultsPageDto {
        return SearchResultsPageDto(results = listOf(), page = 9609, totalPages = 4163)
    }

    override suspend fun getProductDetails(link: String): CardmarketProductDetailsDto {
        return CardmarketProductDetailsDto(
            imageUrl = "https://duckduckgo.com/?q=finibus",
            price = "interdum",
            priceTrend = PriceTrendType("0.00", false),
            detailsUrl = "https://duckduckgo.com/?q=interdum",
            name = NameDto("xxx","de","yy"),
            set = SetDto("xxx","de"),
            genre = "xxx",
            type = "xxx",
            rarity = "xxx",
            cmId = "sdfsfd",
            code = CodeType("xxx", false),
        )
    }
}
