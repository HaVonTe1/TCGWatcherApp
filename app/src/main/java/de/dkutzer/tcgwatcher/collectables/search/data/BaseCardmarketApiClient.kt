package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.search.domain.CardsApiClient
import de.dkutzer.tcgwatcher.collectables.search.domain.CodeType
import de.dkutzer.tcgwatcher.collectables.search.domain.NameDto
import de.dkutzer.tcgwatcher.collectables.search.domain.PriceTrendType
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductDetailsDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketProductGallaryItemDto
import de.dkutzer.tcgwatcher.collectables.search.domain.SearchResultsPageDto
import de.dkutzer.tcgwatcher.collectables.search.domain.CardmarketSellOfferDto
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
            logger.debug { "Parsing: $it" }
            val cmLink = it.attr("href")
            logger.debug { "link: $cmLink" }
            val triple = parseLink(cmLink)

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
            logger.debug { "name: $name code: $code" }


            val intPriceTag = it.getElementsByTag("b")
            logger.debug { "Found intPriceTag: $intPriceTag" }
            val intPrice = intPriceTag.text()
            logger.debug { "Price: $intPrice" }

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

            cardmarketProductGallaryItemDtos.add(itemDto)

        }
        val totalPages = parsePagination(document)


        return SearchResultsPageDto(cardmarketProductGallaryItemDtos, page, totalPages)

    }

    private fun parsePagination(document: Document): Int {
        logger.debug { "Looking for Pagination info" }
        val paginationDiv = document.getElementById("pagination")
        logger.debug { paginationDiv }
        val paginationSpans = paginationDiv?.getElementsByTag("span")
        logger.debug { "Spans: $paginationSpans" }
        val paginationSpan = paginationSpans?.first { s -> s.hasClass("mx-1") }
        logger.debug { "mxSpan: $paginationSpan" }

        var groupValue: String? = null
        if (paginationSpan != null) {
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

    fun parseProductDetails(document: Document, link: String): CardmarketProductDetailsDto {
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
        logger.debug { "name: $name code: $code" }

        val orgName = link.split("/").last()
        logger.debug { "Org Name: $orgName" }

        val typePath =
            document.getElementsByTag("nav").first()?.getElementsByTag("a")?.last { a -> a.hasAttr("href") }?.attr("href")

        val triple = parseLink(typePath)

        val infoDivs = document.getElementsByClass("info-list-container")
        var localPrice = "0,00 €"
        var localPriceTrend = "0,00 €"
        val infoDiv = infoDivs.first()

        val dts = infoDiv?.getElementsByTag("dt")

        val rarityDt = dts?.first { dt -> dt.text() == "Rarität" }
        val rarityText = rarityDt?.nextElementSibling()?.getElementsByTag("svg")?.attr("title")

        if (rarityText != null) {
            logger.debug { "Rarity: $rarityText" }
        }

        val setDt = dts?.first { dt -> dt.text().startsWith("Erschienen") }
        val setHref = setDt?.getElementsByTag("a")
        val setLink = setHref?.first()?.attr("href")
        val setName = setHref?.first()?.attr("title")



        val abDt = dts?.first { dt -> dt.text() == "ab" }
        val abDd = abDt?.nextElementSibling()
        if (abDd != null) {
            localPrice = abDd.text()
        }

        val priceTrendDt = dts?.first { dt -> dt.text() == "Preis-Trend" }
        val priceTrendDd = priceTrendDt?.nextElementSibling()
        if (priceTrendDd != null) {
            val span = priceTrendDd.getElementsByTag("span")
            localPriceTrend = span.text()
        }


        val cardmarketSellOfferDtos = ArrayList<CardmarketSellOfferDto>()

        val sellOfferRows = document.getElementsByClass("article-row")
        logger.debug { "Sell Offer Rows: $sellOfferRows" }
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

        return CardmarketProductDetailsDto(
            name = NameDto(name ?: displayName, triple.first ?: "", displayName),
            code = CodeType(code ?: "", code != null),
            type = triple.third ?: "",
            genre = triple.second ?: "",
            rarity = rarityText?:"",
            set = SetDto(setName ?: "", setLink ?: ""),
            detailsUrl = link,
            imageUrl = imageUrl,
            price =  localPrice,
            priceTrend =  PriceTrendType(localPriceTrend, true),
            sellOffers = cardmarketSellOfferDtos
        )
    }
}

//  /de/pokemon /en/magic etc
private val languageAndGenreAndTypePattern = "//(.*?)/\\((.*?)/\\((.*?)\\)".toRegex()

//private fun extractBreadCrump(document: Document): Triple<String?, String?, String?> {
//    val typePath =
//        document.getElementsByTag("nav").first()?.getElementsByTag("a")?.last { a -> a.hasAttr("href") }?.attr("href")
//    return parseLink(typePath)
//}

private fun parseLink(typePath: String?): Triple<String?, String?, String?> {
    val matchResult = languageAndGenreAndTypePattern.find(typePath ?: "")
    val language = matchResult?.groupValues?.getOrNull(1)
    val genre = matchResult?.groupValues?.getOrNull(2)
    val type = matchResult?.groupValues?.getOrNull(3)
    return Triple(language, genre, type)
}
