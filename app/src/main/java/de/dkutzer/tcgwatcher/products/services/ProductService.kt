package de.dkutzer.tcgwatcher.products.services

import de.dkutzer.tcgwatcher.products.adapter.port.ProductRepository
import de.dkutzer.tcgwatcher.products.domain.SearchProductViewModel
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

