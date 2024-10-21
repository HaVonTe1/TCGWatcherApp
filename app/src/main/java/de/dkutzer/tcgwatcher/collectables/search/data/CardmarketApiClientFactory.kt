package de.dkutzer.tcgwatcher.collectables.search.data

import de.dkutzer.tcgwatcher.settings.domain.BaseConfig
import de.dkutzer.tcgwatcher.settings.domain.Engines
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CardmarketApiClientFactory(val config: BaseConfig) {

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