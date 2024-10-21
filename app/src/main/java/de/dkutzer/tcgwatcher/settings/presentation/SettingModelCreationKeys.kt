package de.dkutzer.tcgwatcher.settings.presentation

import androidx.lifecycle.viewmodel.CreationExtras
import de.dkutzer.tcgwatcher.settings.data.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.domain.Languages
import de.dkutzer.tcgwatcher.settings.domain.SettingsRepository

class SettingModelCreationKeys {
    object LanguagesIdKey : CreationExtras.Key<Map<Languages, String>>
    object SettingsRepoIdKey : CreationExtras.Key<SettingsRepository>
    object SettingsDbIdKey : CreationExtras.Key<SettingsDatabase>
}