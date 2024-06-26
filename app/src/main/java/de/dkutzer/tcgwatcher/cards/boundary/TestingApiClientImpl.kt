package de.dkutzer.tcgwatcher.cards.boundary

import de.dkutzer.tcgwatcher.cards.entity.BaseConfig
import de.dkutzer.tcgwatcher.cards.entity.CardDetailsDto
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemDto
import de.dkutzer.tcgwatcher.cards.entity.SearchResultsPageDto

class TestingApiClientImpl(config: BaseConfig) : BaseCardmarketApiClient() {
    override suspend fun search(searchString: String, page: Int): SearchResultsPageDto {

        //Hint: I use "python3 -m http.server" in the "sampledata" folder to make this work
        val resultItemList = listOf(
            SearchResultItemDto(
                displayName = "Blaues Pokemon",
                orgName = "Blue Pokemon",
                cmLink = "https://localhost:8080/test/bluepoke",
                imgLink = "https://havonte.ddns.net/test/card_blue.png",
                price = "1.00 €"
            ),
            SearchResultItemDto(
                displayName = "Braunes Pokemon",
                orgName = "Brown Pokemon",
                cmLink = "https://localhost:8080/test/brownpoke",
                imgLink = "https://havonte.ddns.net/test/card_braun.png",
                price = "2.00 €"
            ),
            SearchResultItemDto(
                displayName = "Grünes Pokemon",
                orgName = "Green Pokemon",
                cmLink = "https://localhost:8080/test/greenpoke",
                imgLink = "https://havonte.ddns.net/test/card_green.png",
                price = "3.00 €"
            ),
            SearchResultItemDto(
                displayName = "Rotes Pokemon",
                orgName = "Red Pokemon",
                cmLink = "https://localhost:8080/test/redpoke",
                imgLink = "https://havonte.ddns.net/test/card_red.png",
                price = "4.00 €"
            )
        )

        val resultsPageDto = SearchResultsPageDto(
            results = resultItemList,
            page = 1,
            totalPages = 1
        )
        return resultsPageDto
    }

    override suspend fun getProductDetails(link: String): CardDetailsDto {
        TODO("Not yet implemented")
    }

}