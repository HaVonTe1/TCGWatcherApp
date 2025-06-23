package de.dkutzer.tcgwatcher.collectables.search.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface PagingProductsRepository {
    fun getPagingProductsFlow(lang: String): Flow<PagingData<ProductModel>>
}