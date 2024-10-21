package de.dkutzer.tcgwatcher.settings.domain

import de.dkutzer.tcgwatcher.settings.data.SettingsEntity

interface SettingsRepository {
    suspend fun load(): SettingsEntity
    suspend fun save(settingsEntity: SettingsEntity)
    suspend fun updateLanguage(lang: Languages)
    suspend fun updateEngine(engine: Engines)
}