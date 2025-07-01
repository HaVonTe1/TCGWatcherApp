@file:JvmName("PagingProductsRepositoryKt")

package de.dkutzer.tcgwatcher.collectables.search.data

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import de.dkutzer.tcgwatcher.collectables.history.domain.ProductAggregate
import de.dkutzer.tcgwatcher.collectables.search.domain.PagingProductsRepository
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.settings.domain.BaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private val logger = KotlinLogging.logger {}

class GetProductsList(
    private val pagingProductsRepository: PagingProductsRepository,
    private val config: BaseConfig
) {
    operator fun invoke(): Flow<PagingData<ProductModel>> {
        logger.debug { "Update  Flow" }
        return pagingProductsRepository.getPagingProductsFlow(config.lang.name)
            .flowOn(Dispatchers.IO)
    }
}

class PagingProductsRepositoryAdapter(
    private val productsPager: Pager<Int, ProductAggregate>,
) : PagingProductsRepository {
    override fun getPagingProductsFlow(lang: String): Flow<PagingData<ProductModel>> {
        logger.debug { "PagingProductsRepositoryAdapter::getPagingProductsFlow" }
        val dataFlow = productsPager.flow.map { pagingDate ->
            pagingDate.map {
                logger.debug { "PagingProductsRepositoryAdapter::getPagingProductsFlow::map: $it" }
                it.toProductModel(lang)
            }
        }
        return dataFlow
    }

}