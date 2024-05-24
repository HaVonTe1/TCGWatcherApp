package de.dkutzer.tcgwatcher.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.settings.control.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.control.SettingsRepository
import de.dkutzer.tcgwatcher.settings.control.SettingsRepositoryImpl
import de.dkutzer.tcgwatcher.settings.entity.Engines
import de.dkutzer.tcgwatcher.settings.entity.EnginesIdKey
import de.dkutzer.tcgwatcher.settings.entity.Languages
import de.dkutzer.tcgwatcher.settings.entity.LanguagesIdKey
import de.dkutzer.tcgwatcher.settings.entity.SettingsRepoIdKey
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}


@Composable
fun SettingsActivity() {

    val availableLanguages = mapOf(
        Languages.DE to stringResource(id = R.string.german) ,
        Languages.EN to stringResource(id = R.string.english))


    val availableEngines = Engines.values().map { it.displayName }.toList()
    val context = LocalContext.current

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(SettingsDatabase.getDatabase(context).settingsDao)
    }
    


    val settingsViewModel = viewModel<SettingsViewModel>(
        factory = SettingsViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(LanguagesIdKey, availableLanguages)
            set(EnginesIdKey, availableEngines)
            set(SettingsRepoIdKey, settingsRepository)
        }
    )

    val setingsState = settingsViewModel.uiState.collectAsState();
    SettingsView(
        settingsState = setingsState.value,
        availableLanguages = availableLanguages,
        availableEngines = availableEngines,
        onLanguageChanged = {
                val key =
                    availableLanguages.entries.find { entry -> entry.value.compareTo(it) == 0 }?.key
                settingsViewModel.onLanguageChanged(key!!)

        },
        onEngineChanged = {
                val key = Engines.fromDisplayName(it)
                settingsViewModel.onEngineChanged(key!!)

        }
    )
}

@Composable
fun SettingsView(
    settingsState: SettingsState,
    availableLanguages: Map<Languages, String>,
    availableEngines: List<String>,
    onLanguageChanged: (String) -> Unit,
    onEngineChanged: (String) -> Unit
) {

    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {

        //Language
        DropdownSettingsItem(
            availableLanguages.values.toList(),
            settingsState.currentLanguage,
            stringResource(id = R.string.language),
            stringResource(id = R.string.language_desc),
            onLanguageChanged)
        //Engine
        DropdownSettingsItem(availableEngines,
            settingsState.currentEngine,
            stringResource(id = R.string.engine),
            stringResource(id = R.string.engine_desc),
            onEngineChanged)

    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DropdownSettingsItem(
    items: List<String>,
    selectedItem: String,
    label: String,
    labelDesc: String,
    onChanged: (String) -> Unit
)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
    ) {
        val context = LocalContext.current
        var expanded by remember { mutableStateOf(false) }
        val selectedText = remember(selectedItem) { mutableStateOf(selectedItem) }


        Column(
            modifier = Modifier.weight(0.3f)
        ) {
            Text(
                modifier = Modifier
                    .padding(2.dp),
                text = "$label: ",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 20.sp

            )
            Text(
                modifier = Modifier
                    .padding(2.dp),
                text = "$labelDesc: ",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 10.sp

            )
        }

        ExposedDropdownMenuBox(
            modifier = Modifier
                .weight(0.7f)
                .padding(2.dp),
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedText.value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            logger.debug { "DropDown OnClick: $item" }
                            selectedText.value = item
                            expanded = false
                            onChanged(item)
                            Toast.makeText(context, "$label changed to: $item", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

data class SettingsState(val currentLanguage: String = "", val currentEngine: String = "")

class SettingsViewModel(
    val languages: Map<Languages,String>,
    val engines: List<String>,
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()


    fun fetchSettings() {
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
            currentLanguage =  languages.getValue(lang)
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

                val languages = extras[LanguagesIdKey]
                val engines = extras[EnginesIdKey]
                val settingsRepo = extras[SettingsRepoIdKey]
                return SettingsViewModel(
                    languages!!, engines!!, settingsRepo!!
                ) as T
            }
        }
    }

}
@Preview
@Composable
fun SettingsViewPreview() {
    val availableLanguages = mapOf(
        Languages.DE to "German" ,
        Languages.EN to "English")
    val availableEngines = listOf("Steam", "Epic Games")

    SettingsView(
        settingsState = SettingsState(),
        availableLanguages = availableLanguages,
        availableEngines = availableEngines,
        onLanguageChanged = {},
        onEngineChanged = {}
    )
}

