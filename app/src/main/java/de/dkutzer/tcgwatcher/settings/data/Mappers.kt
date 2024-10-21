package de.dkutzer.tcgwatcher.settings.data

import de.dkutzer.tcgwatcher.settings.domain.SettingsModel

fun SettingsEntity.toModel(): SettingsModel {
    return SettingsModel(
        language = language,
        engine = engine
    )
}
