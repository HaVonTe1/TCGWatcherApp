package de.dkutzer.tcgwatcher.products.domain.port

sealed interface SettingsEvent {
    object savedSettings: SettingsEvent

    data class SetLanguage(val lang: String): SettingsEvent
    data class SetEngine(val engine: String) : SettingsEvent

    object showDialog: SettingsEvent
    object hideDialog: SettingsEvent
}