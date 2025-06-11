@file:JvmName("PagingProductsRepositoryKt")

package de.dkutzer.tcgwatcher.collectables.search.data

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductWithSellOffers
import de.dkutzer.tcgwatcher.collectables.search.domain.PagingProductsRepository
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private val logger = KotlinLogging.logger {}

class GetProductsList(
    private val pagingProductsRepository: PagingProductsRepository
) {
    operator fun invoke(): Flow<PagingData<ProductModel>> {
        logger.debug { "Update  Flow" }
        return pagingProductsRepository.getPagingProductsFlow()
            .flowOn(Dispatchers.IO)
    }
}

class PagingProductsRepositoryAdapter(
    private val productsPager: Pager<Int, ProductWithSellOffers>,
) : PagingProductsRepository {
    override fun getPagingProductsFlow(): Flow<PagingData<ProductModel>> {
        logger.debug { "PagingProductsRepositoryAdapter::getPagingProductsFlow" }
        val dataFlow = productsPager.flow.map { pagingDate ->
            pagingDate.map {
                logger.debug { "PagingProductsRepositoryAdapter::getPagingProductsFlow::map: ${it}" }
                it.toProductModel()
            }
        }
        return dataFlow
    }

}