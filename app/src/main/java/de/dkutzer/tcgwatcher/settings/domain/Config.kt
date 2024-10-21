package de.dkutzer.tcgwatcher.settings.domain


data class CardmarketConfig (
    override val baseUrl: String = "https://www.cardmarket.com",
    override val lang: Languages = Languages.DE,
    override val engine: Engines = Engines.HTMLUNIT_NOJS,
    override val searchUrl: String = "$baseUrl/$lang/Pokemon/Products/Search",
    override val limit: Int = 100 // 100 is max - we try to fetch all data and cache it

) : BaseConfig(baseUrl, lang, engine, searchUrl, limit) {
    //holy sh*t, this is tight coupling
    constructor(settingsModel: SettingsModel):
            this(lang = settingsModel.language,
                engine = settingsModel.engine
            )
}

open  class BaseConfig(
    open val baseUrl: String,
    open val lang: Languages,
    open val engine: Engines,
    open val searchUrl : String,
    open val limit: Int
)



