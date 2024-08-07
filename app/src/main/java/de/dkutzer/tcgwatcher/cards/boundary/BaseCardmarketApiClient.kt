package de.dkutzer.tcgwatcher.cards.boundary

import de.dkutzer.tcgwatcher.cards.entity.CardDetailsDto
import de.dkutzer.tcgwatcher.cards.entity.SearchResultItemDto
import de.dkutzer.tcgwatcher.cards.entity.SearchResultsPageDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.nodes.Document

private val logger = KotlinLogging.logger {}


interface CardsApiClient {
    suspend fun search(searchString: String, page: Int = 1): SearchResultsPageDto
    suspend fun search(searchString: String, offset: Int , limit: Int): SearchResultsPageDto {
        return search(searchString, (offset + limit).floorDiv(limit))
    }

    suspend fun getProductDetails(link: String): CardDetailsDto
}
abstract class BaseCardmarketApiClient : CardsApiClient {

    private val paginationRegex = "\\b(?:von|of|de) (\\d+)\\b".toRegex()


    fun parseGallerySearchResults(document: Document, page: Int): SearchResultsPageDto {
       logger.debug { "Parsing a tags with class card and a href" }
        val tiles = document.getElementsByTag("a").filter { element ->  element.hasClass("card") && element.hasAttr("href") }
       logger.debug { "Found: ${tiles.size}" }

        val searchResultItemDtos = ArrayList<SearchResultItemDto>(tiles.size)

        tiles.forEach {
           logger.debug { "Parsing: $it" }
            val cmLink = it.attr("href")
           logger.debug { "link: $cmLink" }

            val imgTag = it.getElementsByTag("img")
           logger.debug { "ImgTag: $imgTag" }
            val imageLink = imgTag.attr("data-echo")
           logger.debug { "Image Link: $imageLink" }

            val titleTag = it.getElementsByTag("h2")
           logger.debug { "TitleTag: $titleTag" }
            val localName = titleTag.text()
           logger.debug { "Local Name: $localName" }

            val intPriceTag = it.getElementsByTag("b")
           logger.debug { "Found intPriceTag: $intPriceTag" }
            val intPrice = intPriceTag.text()
           logger.debug { "Price: $intPrice" }

            val itemDto = SearchResultItemDto(
                displayName = localName,
                orgName = "",
                cmLink = cmLink,
                imgLink = imageLink,
                price = intPrice
            )

            searchResultItemDtos.add(itemDto)

        }
        val totalPages = parsePagination(document)


        return SearchResultsPageDto(searchResultItemDtos, page, totalPages)

    }

    private fun parsePagination(document: Document): Int {
       logger.debug { "Looking for Pagination info" }
        val paginationDiv = document.getElementById("pagination")
       logger.debug { paginationDiv }
        val paginationSpans = paginationDiv?.getElementsByTag("span")
       logger.debug { "Spans: $paginationSpans" }
        val paginationSpan = paginationSpans?.first { s -> s.hasClass("mx-1") }
       logger.debug { "mxSpan: $paginationSpan" }

        var groupValue:  String? = null
        if(paginationSpan!=null) {
            val text = paginationSpan.text()
           logger.debug { "Text: $text" }
            val matchResult = paginationRegex.find(text)
            groupValue = matchResult?.groupValues?.getOrNull(1)
           logger.debug { "$groupValue" }
        }

        val totalPages = groupValue?.toInt() ?: 0
       logger.debug { "Found: $totalPages" }
        return totalPages
    }

    fun parseProductDetails(document: Document): CardDetailsDto {
        val imageTags = document.getElementsByTag("img")
        val frontImageTag =
            imageTags.first { img -> img.classNames().size == 1 } //filter out "lazy" img tags
        val imageUrl = frontImageTag.attr("src")

        val infoDivs = document.getElementsByClass("info-list-container")
        var localPrice = "0,00 €"
        var localPriceTrend = "0,00 €"
        if (infoDivs.size == 1) {
            val infoDiv = infoDivs.first()
            val dts = infoDiv?.getElementsByTag("dt")
            val abDt = dts?.first { dt -> dt.text().equals("ab") }
            val abDd = abDt?.nextElementSibling()
            if (abDd != null) {
                localPrice = abDd.text()
            }

            val priceTrendDt = dts?.first { dt -> dt.text().equals("Preis-Trend") }
            val priceTrendDd = priceTrendDt?.nextElementSibling()
            if (priceTrendDd != null) {
                val span = priceTrendDd.getElementsByTag("span")
                localPriceTrend = span.text()
            }

        }

        return CardDetailsDto(imageUrl, localPrice, localPriceTrend)
    }
}