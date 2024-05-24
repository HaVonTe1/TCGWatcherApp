package de.dkutzer.tcgwatcher.cards.entity



data class SearchProductModel(
    override val id: String,
    override val localName: String,
    override val imageUrl: String,
    override val detailsUrl: String,
    override val intPrice: String
) : BaseProductModel(id, imageUrl, detailsUrl, localName, intPrice)


open class BaseProductModel(
    open val id: String,
    open val imageUrl: String,
    open val detailsUrl: String,
    open val localName: String,
    open val intPrice: String
)


data class SearchItem(
    val displayName : String,
    val orgName: String,
    val cmLink: String,
    val imgLink: String,
    val price : String
)

data class SearchResults(
    val items: List<SearchItem>,
    val currentPage: Int,
    val pages: Int
)
