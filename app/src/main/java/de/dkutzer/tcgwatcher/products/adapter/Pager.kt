package de.dkutzer.tcgwatcher.products.adapter

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.Room
import de.dkutzer.tcgwatcher.products.adapter.api.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.products.domain.SearchResultItemEntity
import de.dkutzer.tcgwatcher.products.domain.port.SearchCacheDatabase
import de.dkutzer.tcgwatcher.products.domain.port.SettingsDatabase
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}


abstract class PokemonPager {
    //Highlander Pattern
    companion object {
        @Volatile
        private var Instance: Pager<Int, SearchResultItemEntity>? = null

        @OptIn(ExperimentalPagingApi::class)
        fun providePokemonPager(
            searchTerm: String,
            pokemonDatabase: SearchCacheDatabase,
            pokemonApi: BaseCardmarketApiClient,
        ): Pager<Int, SearchResultItemEntity> {

            logger.debug { "create Pokemon Pager" }
            return Instance ?: synchronized(this) {
                return Pager(
                    config = PagingConfig(pageSize = 5),
                    remoteMediator = SearchRemoteMediator(
                        searchTerm = searchTerm,
                        pokemonDatabase = pokemonDatabase,
                        pokemonApi = pokemonApi,
                    ),
                    pagingSourceFactory = {
                        pokemonDatabase.searchCacheDaoDa.pagingSource(searchTerm)
                    },
                )
            }
        }
    }

}

