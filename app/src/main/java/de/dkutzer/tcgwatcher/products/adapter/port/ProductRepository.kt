package de.dkutzer.tcgwatcher.products.adapter.port

import android.net.Uri
import de.dkutzer.tcgwatcher.products.domain.model.ProductModel
import de.dkutzer.tcgwatcher.products.domain.model.SearchItem

interface ProductRepository {

    fun getProductImageUrlById(link: String) : String

    fun search(searchString : String) : List<SearchItem>
}