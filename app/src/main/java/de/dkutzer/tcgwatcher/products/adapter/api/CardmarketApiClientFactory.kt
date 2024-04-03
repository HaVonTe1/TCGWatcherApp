package de.dkutzer.tcgwatcher.products.adapter.api

import de.dkutzer.tcgwatcher.products.config.BaseConfig
import de.dkutzer.tcgwatcher.products.domain.Engines
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CardmarketApiClientFactory(val config: BaseConfig) {

    fun create(): BaseCardmarketApiClient  {
        logger.debug { "Creating new client with : ${config.engine}" }
        return when(config.engine) {
            Engines.HTMLUNIT_JS -> CardmarketHtmlUnitApiClientImpl(config)
            Engines.HTMLUNIT_NOJS -> CardmarketHtmlUnitApiClientImpl(config)
            Engines.KTOR -> CardmarketKtorApiClientImpl(config)
            Engines.TESTING -> TestingApiClientImpl(config)

        }
    }
}