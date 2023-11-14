package de.dkutzer.tcgwatcher.products.config



data class CardmarketConfig (
    override val baseUrl: String = "https://www.cardmarket.com",
    override val lang: String = "de",
    override val searchUrl: String = "$baseUrl/$lang/Pokemon/Products/Search"
) : BaseConfig(baseUrl, lang, searchUrl)

open  class BaseConfig(open val baseUrl: String, open val lang: String, open val searchUrl : String)



