package de.dkutzer.tcgwatcher.products.services

import android.icu.util.CurrencyAmount
import de.dkutzer.tcgwatcher.products.adapter.port.ProductRepository
import java.time.OffsetDateTime

class ProductService(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper
) {

    fun search(searchString: String, page: Int) : List<ProductModel> {
        val searchResults = productRepository.search(searchString, page)
        val productModels = searchResults.items.map {

            val productDetails = productRepository.getProductDetails(it.cmLink)
            productMapper.toModel(it, productDetails)
        }.toList()
        return productModels
    }
}

data class ProductModel(
    val id : String,
    val imageUrl : String,
    val detailsUrl : String,
    val details: ProductDetailsModel
)

data class ProductDetailsModel(
    val localName: String,
    val intName: String,
    val price: CurrencyAmount,
    val localPrice: CurrencyAmount,
    val localPriceTrend: CurrencyAmount,
    val lastUpdate: OffsetDateTime
)

data class SearchItem(
    val displayName : String,
    val orgName: String,
    val cmLink: String,
    val price : CurrencyAmount
)

data class SearchResults(
    val items: List<SearchItem>,
    val currentPage: Int,
    val pages: Int
)
