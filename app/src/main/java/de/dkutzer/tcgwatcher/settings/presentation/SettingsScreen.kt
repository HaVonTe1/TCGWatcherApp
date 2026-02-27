package de.dkutzer.tcgwatcher.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.settings.data.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.data.SettingsRepositoryImpl
import de.dkutzer.tcgwatcher.settings.domain.Engines
import de.dkutzer.tcgwatcher.settings.domain.Languages
import de.dkutzer.tcgwatcher.settings.domain.SettingsRepository
import de.dkutzer.tcgwatcher.settings.presentation.components.SettingsView
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme

@Composable
fun SettingsScreen() {

    val availableLanguages = mapOf(
        Languages.DE to stringResource(id = R.string.german) ,
        Languages.EN to stringResource(id = R.string.english))


    val availableEngines = Engines.entries.map { it.displayName }.toList()
    val context = LocalContext.current

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(SettingsDatabase.getDatabase(context).settingsDao)
    }
    


    val settingsViewModel = viewModel<SettingsViewModel>(
        factory = SettingsViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(SettingModelCreationKeys.LanguagesIdKey, availableLanguages)
            set(SettingModelCreationKeys.SettingsRepoIdKey, settingsRepository)
        }
    )

    val settingsState = settingsViewModel.uiState.collectAsState()
    SettingsView(
        settingsState = settingsState.value,
        availableLanguages = availableLanguages,
        availableEngines = availableEngines,
        onLanguageChanged = { displayName ->
                val key = availableLanguages.entries.find { entry -> entry.value.compareTo(displayName) == 0 }?.key
                key?.let { settingsViewModel.onLanguageChanged(it) }

        },
        onEngineChanged = { engineDisplayName ->
                val key = Engines.fromDisplayName(engineDisplayName)
                key?.let { settingsViewModel.onEngineChanged(it) }

        }
    )
}

data class SettingsState(val currentLanguage: String = "", val currentEngine: String = "")

@Preview
@Composable
fun SettingsViewPreview() {
    val availableLanguages = mapOf(
        Languages.DE to "German" ,
        Languages.EN to "English")
    val availableEngines = listOf("Steam", "Epic Games")

    TCGWatcherTheme {

        SettingsView(
            settingsState = SettingsState(),
            availableLanguages = availableLanguages,
            availableEngines = availableEngines,
            onLanguageChanged = {},
            onEngineChanged = {}
        )

    }
}

