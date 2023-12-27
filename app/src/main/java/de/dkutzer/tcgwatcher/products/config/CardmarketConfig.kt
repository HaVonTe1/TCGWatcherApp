package de.dkutzer.tcgwatcher.products.config

import de.dkutzer.tcgwatcher.products.domain.Engines
import de.dkutzer.tcgwatcher.products.domain.Languages
import de.dkutzer.tcgwatcher.products.domain.SettingsEntity


data class CardmarketConfig (
    override val baseUrl: String = "https://www.cardmarket.com",
    override val lang: Languages = Languages.DE,
    override val engine: Engines = Engines.HTMLUNIT_NOJS,
    override val searchUrl: String = "$baseUrl/$lang/Pokemon/Products/Search",

) : BaseConfig(baseUrl, lang, engine, searchUrl) {
    //holy sh*t this is bad coupling
    constructor(settingsEntity: SettingsEntity): this(lang = settingsEntity.language, engine = settingsEntity.engine)
}

open  class BaseConfig(
    open val baseUrl: String,
    open val lang: Languages,
    open val engine: Engines,
    open val searchUrl : String)



