package de.dkutzer.tcgwatcher.collectables.search.data.cardmarket

import de.dkutzer.tcgwatcher.collectables.search.data.TestingApiClientImpl
import de.dkutzer.tcgwatcher.settings.data.cardmarket.CardmarketConfig
import de.dkutzer.tcgwatcher.settings.domain.Engines
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CardmarketApiClientFactory(val config: CardmarketConfig) {

    fun create(): BaseCardmarketApiClient {
        logger.debug { "Creating new client with : ${config.engine}" }
        return when(config.engine) {
            Engines.HTMLUNIT_JS -> CardmarketHtmlUnitApiClientImpl(config)
            Engines.HTMLUNIT_NOJS -> CardmarketHtmlUnitApiClientImpl(config)
            Engines.KTOR -> CardmarketKtorApiClientImpl(config)
            Engines.TESTING -> TestingApiClientImpl()

        }
    }
}

