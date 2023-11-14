package de.dkutzer.tcgwatcher.products.services

import de.dkutzer.tcgwatcher.products.adapter.port.ProductRepository

class ProductService  {

    val productRepository : ProductRepository

    constructor(productRepository: ProductRepository) {
        this.productRepository = productRepository
    }


}