package de.dkutzer.tcgwatcher.cards.boundary

import de.dkutzer.tcgwatcher.cards.entity.BaseConfig
import de.dkutzer.tcgwatcher.settings.entity.Engines
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}


class CardmarketApiClientFactory(val config: BaseConfig) {

    fun create(): BaseCardmarketApiClient {
        logger.debug { "Creating new client with : ${config.engine}" }
        return when(config.engine) {
            Engines.HTMLUNIT_JS -> CardmarketHtmlUnitApiClientImpl(config)
            Engines.HTMLUNIT_NOJS -> CardmarketHtmlUnitApiClientImpl(config)
            Engines.KTOR -> CardmarketKtorApiClientImpl(config)
            Engines.TESTING -> TestingApiClientImpl(config)

        }
    }
}