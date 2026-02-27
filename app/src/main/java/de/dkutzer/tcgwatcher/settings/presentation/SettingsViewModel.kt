package de.dkutzer.tcgwatcher.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.dkutzer.tcgwatcher.settings.domain.Engines
import de.dkutzer.tcgwatcher.settings.domain.Languages
import de.dkutzer.tcgwatcher.settings.domain.SettingsRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}


class SettingsViewModel(
    private val languages: Map<Languages,String>,
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()


    private fun fetchSettings() {
        logger.info { "Init SettingsViewModel" }

        viewModelScope.launch(Dispatchers.IO) {
            val settingsEntity = settingsRepository.load()
            logger.debug { "Current Settings: $settingsEntity" }

            _uiState.value = SettingsState(
                currentEngine = settingsEntity.engine.displayName,
                currentLanguage = languages.getValue(settingsEntity.language)
            )
            logger.debug { "Current State: ${uiState.value}" }

        }
        logger.debug { "Finished Init SettingsViewModel" }

    }
    // Initialize ViewModel
    init {
        logger.debug { "Init SettingsViewModel" }
        fetchSettings()
        logger.debug { "Finished Init SettingsViewModel" }
    }

    fun onLanguageChanged(lang: Languages) {
        logger.info { "Updating settings Language with: $lang" }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateLanguage(lang)
        }
        _uiState.value = SettingsState(
            currentEngine = uiState.value.currentEngine,
            currentLanguage = languages.getValue(lang)
        )
    }

    fun onEngineChanged(engine: Engines) {
        logger.info { "Updating settings engine with $engine" }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateEngine(engine)
        }
        _uiState.value = SettingsState(
            currentEngine = engine.displayName,
            currentLanguage = uiState.value.currentLanguage
        )
    }

    // Define ViewModel factory in a companion object
    companion object {

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {

                val languages = extras[SettingModelCreationKeys.LanguagesIdKey]
                val settingsRepo = extras[SettingModelCreationKeys.SettingsRepoIdKey]
                return SettingsViewModel(
                    requireNotNull(languages) { "Languages not provided in ViewModel creation extras" },
                    requireNotNull(settingsRepo) { "SettingsRepo not provided in ViewModel creation extras" }
                ) as T
            }
        }
    }

}