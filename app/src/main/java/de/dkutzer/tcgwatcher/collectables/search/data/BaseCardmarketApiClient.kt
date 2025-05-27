package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductGallaryItemDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketSellOfferDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardsApiClient
import de.dkutzer.tcgwatcher.collectables.search.domain.CodeType
import de.dkutzer.tcgwatcher.collectables.search.domain.NameDto
import de.dkutzer.tcgwatcher.collectables.search.domain.PriceTrendType
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SetDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.nodes.Document

private val logger = KotlinLogging.logger {}


abstract class BaseCardmarketApiClient : CardsApiClient {

    private val paginationRegex = "\\b(?:von|of|de) (\\d+)\\b".toRegex()

    //Knospi (PRE 004) --> Name: Knospi   code: (PRE-004)
    private val nameAndCodePattern = "^(.*?)\\s*\\((.*?)\\)$".toRegex()


    fun parseGallerySearchResults(document: Document, page: Int): SearchResultsPageDto {


        logger.debug { "Parsing a tags with class card and a href" }
        val tiles = document.getElementsByTag("a")
            .filter { element -> element.hasClass("card") && element.hasAttr("href") }
        logger.debug { "Found: ${tiles.size}" }

        val cardmarketProductGallaryItemDtos = ArrayList<CardmarketProductGallaryItemDto>(tiles.size)

        tiles.forEach {
            val cmLink = it.attr("href")
            val triple = parseLink(cmLink)
            val imgTag = it.getElementsByTag("img")
            val imageLink = imgTag.attr("data-echo")
            val titleTag = it.getElementsByTag("h2")
            val localName = titleTag.text()
            val matchResult = nameAndCodePattern.find(localName)
            val name = matchResult?.groupValues?.getOrNull(1)
            val code = matchResult?.groupValues?.getOrNull(2)
            val intPriceTag = it.getElementsByTag("b")
            val intPrice = intPriceTag.text()
            val itemDto = CardmarketProductGallaryItemDto(
                name = NameDto(name ?: localName, triple.first?:"", localName),
                code = CodeType(code ?: "", code != null),
                genre = triple.second?:"",
                type = triple.third?:"",
                cmLink = cmLink,
                imgLink = imageLink,
                price = intPrice,
                priceTrend = PriceTrendType("?", false)
            )
            logger.debug { "Item: $itemDto" }

            cardmarketProductGallaryItemDtos.add(itemDto)

        }
        val totalPages = parsePagination(document)


        return SearchResultsPageDto(cardmarketProductGallaryItemDtos, page, totalPages)

    }

    private fun parsePagination(document: Document): Int {
        logger.debug { "Looking for Pagination info" }
        val paginationDiv = document.getElementById("pagination")
        val paginationSpans = paginationDiv?.getElementsByTag("span")
        val paginationSpan = paginationSpans?.first { s -> s.hasClass("mx-1") }

        var groupValue: String? = null
        if (paginationSpan != null) {
            val text = paginationSpan.text()
            val matchResult = paginationRegex.find(text)
            groupValue = matchResult?.groupValues?.getOrNull(1)
        }

        val totalPages = groupValue?.toInt() ?: 0
        logger.debug { "Found: $totalPages" }
        return totalPages
    }

    fun parseProductDetails(document: Document, link: String): CardmarketProductDetailsDto {
        val imageTags = document.getElementsByTag("img")
        val frontImageTag =
            imageTags.first { img -> img.classNames().size == 1 } //filter out "lazy" img tags
        val imageUrl = frontImageTag.attr("src")
        val h1Tags = document.getElementsByTag("h1")
        val h1Tag = h1Tags.first()
        val displayName = h1Tag?.ownText() ?: ""

        val matchResult = nameAndCodePattern.find(displayName)
        val name = displayName
        val code = matchResult?.groupValues?.getOrNull(2)
        val orgName = link.split("/").last()
        val typePath =
            document.getElementsByTag("nav").first()?.getElementsByTag("a")?.last { a -> a.hasAttr("href") }?.attr("href")

        val triple = parseLink(typePath)

        val infoDivs = document.getElementsByClass("info-list-container")
        val infoDiv = infoDivs.first()

        val dts = infoDiv?.getElementsByTag("dt")

        val rarityDt = dts?.first { dt -> dt.text() == "Rarität" }
        val rarityText = rarityDt?.nextElementSibling()?.getElementsByTag("svg")?.attr("title")

        val setDt = dts?.first { dt -> dt.text().startsWith("Erschienen") }
        val setHref = setDt?.nextElementSibling()?.getElementsByTag("a")
        val setLink = setHref?.first()?.attr("href")
        val setName = setHref?.first()?.attr("title")

        val abDt = dts?.first { dt -> dt.text() == "ab" }
        val localPrice = abDt?.nextElementSibling()?.text() ?: "0,00 €"

        val priceTrendDt = dts?.first { dt -> dt.text() == "Preis-Trend" }
        val localPriceTrend = priceTrendDt?.nextElementSibling()?.getElementsByTag("span")?.text() ?: "0,00 €"

        val cardmarketSellOfferDtos = ArrayList<CardmarketSellOfferDto>()

        val sellOfferRows = document.getElementsByClass("article-row")
        sellOfferRows.forEach { sellOfferRow ->
            val sellerCol = sellOfferRow.getElementsByClass("col-seller").first()
            val sellerHrefTag = sellerCol?.getElementsByTag("a")
            val sellerName = sellerHrefTag?.text()

            val productLocationTag = sellerCol?.getElementsByClass("icon")
            val productLocation = productLocationTag?.first()?.attr("title")

            val productAttributesDiv = sellOfferRow?.getElementsByClass("product-attributes")

            val productCondition = productAttributesDiv?.first()?.getElementsByClass("article-condition")?.first()
                ?.attr("title")

            val productAttributIcons = productAttributesDiv?.first()?.getElementsByClass("icon")
            val productLanguage = productAttributIcons?.first()
                ?.attr("title")
            var productSpeciality = ""
            productAttributIcons?.size?.let {
                if(it>1) {
                    productSpeciality = productAttributIcons[1]
                        .attr("title")
                }
            }

            val priceContainer = sellOfferRow.getElementsByClass("price-container").first()
            val price = priceContainer?.getElementsByTag("span")?.text()

            val productAmount = sellOfferRow?.getElementsByClass("amount-container")?.first()?.getElementsByTag("span")?.first()?.text()

            if(sellerName!=null && productLocation!=null && productLanguage!=null && price!=null && productAmount!=null && productCondition!=null) {
                val cardmarketSellOfferDto = CardmarketSellOfferDto(
                    sellerName = sellerName,
                    sellerLocation = productLocation,
                    productLanguage = productLanguage,
                    special = productSpeciality,
                    condition = productCondition,
                    amount = productAmount,
                    price = price
                )
                logger.debug { "Sell Offer: $cardmarketSellOfferDto" }
                cardmarketSellOfferDtos.add(cardmarketSellOfferDto)

            }
        }

        val cardmarketProductDetailsDto = CardmarketProductDetailsDto(
            name = NameDto(value =  name ?: displayName, languageCode = triple.first ?: "", i18n =  orgName),
            code = CodeType(code ?: "", code != null),
            type = triple.third ?: "",
            genre = triple.second ?: "",
            rarity = rarityText ?: "",
            set = SetDto(setName ?: "", setLink ?: ""),
            detailsUrl = link,
            imageUrl = imageUrl,
            price = localPrice,
            priceTrend = PriceTrendType(localPriceTrend, true),
            sellOffers = cardmarketSellOfferDtos
        )
        logger.debug { "Product Details: $cardmarketProductDetailsDto" }
        return cardmarketProductDetailsDto
    }
}

//  /de/pokemon /en/magic etc
private val languageAndGenreAndTypePattern = "^\\s*/?([^/]+)/([^/]+)/[^/]+/([^/]+)".toRegex()

fun parseLink(typePath: String?): Triple<String?, String?, String?> {
    logger.debug { "Parsing Link: $typePath" }
    val matchResult = languageAndGenreAndTypePattern.find(typePath ?: "")
    val language = matchResult?.groupValues?.getOrNull(1)
    val genre = matchResult?.groupValues?.getOrNull(2)
    val type = matchResult?.groupValues?.getOrNull(3)
    val triple = Triple(language, genre, type)
    logger.debug { "Parsed Link: $triple" }
    return triple
}
