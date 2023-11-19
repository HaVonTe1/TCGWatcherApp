package de.dkutzer.tcgwatcher.products.services

import de.dkutzer.tcgwatcher.products.adapter.port.ProductRepository
import de.dkutzer.tcgwatcher.products.domain.model.ProductModel

class ProductService(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper
) {

    fun search(searchString: String) : List<ProductModel> {
        val searchItems = productRepository.search(searchString)
        val productModels = searchItems.map {
            val imageUrlById = productRepository.getProductImageUrlById(it.cmLink)
            productMapper.toModel(it, imageUrlById)
        }.toList()
        return productModels
    }
}