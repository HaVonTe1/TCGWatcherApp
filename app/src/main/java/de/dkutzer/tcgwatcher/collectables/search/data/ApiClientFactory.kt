package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.collectables.search.data.cardmarket.CardmarketApiClientFactory
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductsApiClient
import de.dkutzer.tcgwatcher.settings.data.cardmarket.CardmarketConfig
import de.dkutzer.tcgwatcher.settings.domain.BaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class ApiClientFactory(val config: BaseConfig) {
    fun create(): ProductsApiClient {
        logger.debug { "Creating new client with : ${config.engine}" }

        if(config is CardmarketConfig) {
            return CardmarketApiClientFactory(config).create()


        }
        throw IllegalArgumentException("Unknown Config: $config")

    }
}