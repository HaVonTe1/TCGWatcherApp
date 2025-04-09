package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.search.domain.CardDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CodeType
import de.dkutzer.tcgwatcher.collectables.search.domain.OrgNameType
import de.dkutzer.tcgwatcher.collectables.search.domain.PriceTrendType
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultItemDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto

class TestingApiClientImpl : BaseCardmarketApiClient() {
    override suspend fun search(searchString: String, page: Int): SearchResultsPageDto {

        //Hint: I use "python3 -m http.server" in the "sampledata" folder to make this work
        val resultItemList = listOf(
            SearchResultItemDto(
                displayName = "Blaues Pokemon",
                code = CodeType("TST 1", true),
                orgName = OrgNameType("Blue Pokemon",true),
                cmLink = "https://localhost:8080/test/bluepoke",
                imgLink = "https://havonte.ddns.net/test/card_blue.png",
                price = "1.00 €",
                priceTrend = PriceTrendType("2.00 €", true)
            ),
            SearchResultItemDto(
                displayName = "Braunes Pokemon",
                code = CodeType("TST 2", true),
                orgName = OrgNameType("Brown Pokemon",true),
                cmLink = "https://localhost:8080/test/brownpoke",
                imgLink = "https://havonte.ddns.net/test/card_braun.png",
                price = "2.00 €",
                priceTrend =  PriceTrendType("1.00 €",true)
            ),
            SearchResultItemDto(
                displayName = "Grünes Pokemon",
                code = CodeType("TST 3",true),
                orgName = OrgNameType("Green Pokemon",true),
                cmLink = "https://localhost:8080/test/greenpoke",
                imgLink = "https://havonte.ddns.net/test/card_green.png",
                price = "3.00 €",
                priceTrend = PriceTrendType("1.00 €",true)
            ),
            SearchResultItemDto(
                displayName = "Rotes Pokemon",
                orgName = "Red Pokemon",
                code = "TST 4",
                cmLink = "https://localhost:8080/test/redpoke",
                imgLink = "https://havonte.ddns.net/test/card_red.png",
                price = "4.00 €",
                priceTrend = "1.00 €"
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