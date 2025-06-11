package de.dkutzer.tcgwatcher.settings.data.cardmarket

import de.dkutzer.tcgwatcher.settings.domain.BaseConfig
import de.dkutzer.tcgwatcher.settings.domain.Engines
import de.dkutzer.tcgwatcher.settings.domain.Languages
import de.dkutzer.tcgwatcher.settings.domain.SettingsModel

data class CardmarketConfig(
    override val baseUrl: String = "https://www.cardmarket.com",
    override val lang: Languages = Languages.DE,
    override val engine: Engines = Engines.HTMLUNIT_NOJS,
    override val searchUrl: String = "$baseUrl/$lang/Pokemon/Products/Search",
    override val limit: Int = 100 // 100 is max - we try to fetch all data and cache it

) : BaseConfig(baseUrl, lang, engine, searchUrl, limit) {
    constructor(settingsModel: SettingsModel) :
            this(
                lang = settingsModel.language,
                engine = settingsModel.engine
            )
}