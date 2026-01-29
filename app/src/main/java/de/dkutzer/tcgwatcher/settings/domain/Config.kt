package de.dkutzer.tcgwatcher.settings.domain

import de.dkutzer.tcgwatcher.settings.data.cardmarket.CardmarketConfig


open class BaseConfig(
    open val baseUrl: String,
    open val lang: Languages,
    open val engine: Engines,
    open val searchUrl: String,
    open val limit: Int,
    val ttlInSeconds: Long = (3 * 24 * 60 * 60L)
)


class ConfigFactory(private val settingsModel: SettingsModel) {

    fun create(): BaseConfig {
        when (settingsModel.datasource) {
            Datasources.CARDMARKET -> return CardmarketConfig(settingsModel)
        }
    }
}
