package de.dkutzer.tcgwatcher.settings.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.settings.domain.Languages
import de.dkutzer.tcgwatcher.settings.presentation.SettingsState
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme

@Composable
fun SettingsView(
    settingsState: SettingsState,
    availableLanguages: Map<Languages, String>,
    availableEngines: List<String>,
    onLanguageChanged: (String) -> Unit,
    onEngineChanged: (String) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {

        //Language
        DropdownSettingsItem(
            availableLanguages.values.toList(),
            settingsState.currentLanguage,
            stringResource(id = R.string.language),
            stringResource(id = R.string.language_desc),
            onLanguageChanged
        )
        //Engine
        DropdownSettingsItem(
            availableEngines,
            settingsState.currentEngine,
            stringResource(id = R.string.engine),
            stringResource(id = R.string.engine_desc),
            onEngineChanged
        )

    }
}

@Composable
@PreviewLightDark

fun SettingsViewPreview(modifier: Modifier = Modifier) {
    TCGWatcherTheme {
        SettingsView(
            settingsState = SettingsState(
                currentLanguage = "English",
                currentEngine = "Engine 1"
            ),
            availableLanguages = mapOf(
                Languages.EN to "English",
                Languages.DE to "Deutsch"
            ),
            availableEngines = listOf("Engine 1", "Engine 2"),
            onLanguageChanged = {},
        ) { }
    }
}