package de.dkutzer.tcgwatcher.products.domain.port

data class SettingsState(
    val lang: String = "de",
    val isJsEnabled: Boolean = false
)
