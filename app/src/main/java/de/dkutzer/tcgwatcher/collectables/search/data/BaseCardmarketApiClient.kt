package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.search.domain.CardDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultItemDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardsApiClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.nodes.Document

private val logger = KotlinLogging.logger {}


abstract class BaseCardmarketApiClient : CardsApiClient {

    private val paginationRegex = "\\b(?:von|of|de) (\\d+)\\b".toRegex()

    private val nameAndCodePattern = "^(.*?)\\s*\\((.*?)\\)$".toRegex()

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

            val matchResult = nameAndCodePattern.find(localName)
            val name = matchResult?.groupValues?.getOrNull(1)
            val code = matchResult?.groupValues?.getOrNull(2)
            logger.debug { "name: $name code: $code"  }


            val intPriceTag = it.getElementsByTag("b")
           logger.debug { "Found intPriceTag: $intPriceTag" }
            val intPrice = intPriceTag.text()
           logger.debug { "Price: $intPrice" }

            val itemDto = SearchResultItemDto(
                displayName = name ?: localName,
                code = code ?: "",
                orgName = "---",
                cmLink = cmLink,
                imgLink = imageLink,
                price = intPrice,
                priceTrend = "?"
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

    fun parseProductDetails(document: Document, link: String): CardDetailsDto {
        val imageTags = document.getElementsByTag("img")
        val frontImageTag =
            imageTags.first { img -> img.classNames().size == 1 } //filter out "lazy" img tags
        val imageUrl = frontImageTag.attr("src")

        logger.debug { "Image URL: $imageUrl" }

        val h1Tags = document.getElementsByTag("h1")
        val h1Tag = h1Tags.first()
        val displayName = h1Tag?.ownText() ?: ""

        logger.debug { "Display Name: $displayName" }

        val matchResult = nameAndCodePattern.find(displayName)
        val name = matchResult?.groupValues?.getOrNull(1)
        val code = matchResult?.groupValues?.getOrNull(2)
        logger.debug { "name: $name code: $code"  }


        val orgName = link.split("/").last()
        logger.debug { "Org Name: $orgName" }

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

        return CardDetailsDto(displayName =name ?: displayName, code = code ?: "", orgName =orgName, imageUrl,link, localPrice, localPriceTrend)
    }
}