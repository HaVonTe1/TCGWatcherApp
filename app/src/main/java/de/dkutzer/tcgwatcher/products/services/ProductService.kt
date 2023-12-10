package de.dkutzer.tcgwatcher.products.services

import de.dkutzer.tcgwatcher.products.adapter.port.ProductRepository
import java.time.OffsetDateTime

class ProductService(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper
) {

    suspend fun search(searchString: String, page: Int) : SearchProductViewModel {
        val searchResults = productRepository.search(searchString, page)
        val productModels = searchResults.items.map {
            productMapper.toModel(it)
        }.toList()
        return SearchProductViewModel(productModels, searchResults.pages)
    }
}

data class SearchProductViewModel(
    val products: List<SearchProductModel>,
    val pages: Int
)

data class SearchProductModel(
    override val id: String,
    override val localName: String,
    override val imageUrl: String,
    override val detailsUrl: String,
    override val intPrice: String
) : BaseProductModel(id, imageUrl, detailsUrl, localName, intPrice)
data class ProductModel(
    override val id : String,
    override val imageUrl : String,
    override val detailsUrl : String,
    val details: ProductDetailsModel
): BaseProductModel(id, imageUrl, detailsUrl, details.localName, details.price)

open class BaseProductModel(
    open val id: String,
    open val imageUrl: String,
    open val detailsUrl: String,
    open val localName: String,
    open val intPrice: String
)

data class ProductDetailsModel(
    val localName: String,
    val intName: String,
    val price: String,
    val localPrice: String,
    val localPriceTrend: String,
    val lastUpdate: OffsetDateTime
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
