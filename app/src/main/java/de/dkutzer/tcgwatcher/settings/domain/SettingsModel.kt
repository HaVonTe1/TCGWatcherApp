package de.dkutzer.tcgwatcher.settings.domain

data class SettingsModel(val language: Languages = Languages.EN,
                         val engine: Engines = Engines.KTOR,
                         val datasource: Datasources = Datasources.CARDMARKET) {
}

enum class Datasources {
    CARDMARKET,
    //TODO: add more datasources
}