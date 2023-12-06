package de.dkutzer.tcgwatcher.products.adapter

import de.dkutzer.tcgwatcher.products.adapter.api.ProductApiClient
import de.dkutzer.tcgwatcher.products.adapter.port.ProductRepository
import de.dkutzer.tcgwatcher.products.adapter.port.toSearchItem
import de.dkutzer.tcgwatcher.products.services.SearchResults


class ProductCardmarketRepositoryAdapter(val client: ProductApiClient) :
    ProductRepository {

    override fun getProductDetails(link: String) = client.getProductDetails(link)



    override fun search(searchString: String, page: Int): SearchResults {
        val searchResults = client.search(searchString, page)

        val searchItems = searchResults.results.map { it.toSearchItem() }.toList()

        return SearchResults(searchItems, page, searchResults.totalPages);
    }
}

