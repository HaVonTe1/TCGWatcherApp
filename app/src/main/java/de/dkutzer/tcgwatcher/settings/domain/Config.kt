package de.dkutzer.tcgwatcher.settings.domain

import de.dkutzer.tcgwatcher.settings.data.cardmarket.CardmarketConfig

const val DEFAULT_TTL_SECONDS = 259200L // 3 days in seconds


open class BaseConfig(
    open val baseUrl: String,
    open val lang: Languages,
    open val engine: Engines,
    open val searchUrl: String,
    open val limit: Int,
    open val ttlInSeconds: Long = DEFAULT_TTL_SECONDS
)


class ConfigFactory(private val settingsModel: SettingsModel) {

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    fun create(): BaseConfig {
        return when (settingsModel.datasource) {
            Datasources.CARDMARKET -> CardmarketConfig(settingsModel)
            else -> throw IllegalArgumentException("Unknown datasource: ${settingsModel.datasource}")
        }
    }
}
