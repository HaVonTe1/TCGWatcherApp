package de.dkutzer.tcgwatcher.collectables.search.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface PagingProductsRepository {
    fun getPagingProductsFlow(): Flow<PagingData<ProductModel>>
}