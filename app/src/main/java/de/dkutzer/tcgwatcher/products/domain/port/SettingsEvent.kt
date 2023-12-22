package de.dkutzer.tcgwatcher.products.domain.port

sealed interface SettingsEvent {
    object savedSettings: SettingsEvent

    data class SetLanguage(val lang: String): SettingsEvent
    data class SetJsEnabled(val enabled: Boolean) : SettingsEvent

    object showDialog: SettingsEvent
    object hideDialog: SettingsEvent
}