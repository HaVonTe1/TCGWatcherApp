package de.dkutzer.tcgwatcher.views

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.products.domain.*
import de.dkutzer.tcgwatcher.products.domain.port.SettingsDatabase
import de.dkutzer.tcgwatcher.products.domain.port.SettingsRepository
import de.dkutzer.tcgwatcher.products.domain.port.SettingsRepositoryImpl
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

    SettingsView(
        viewModel = settingsViewModel,
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
    viewModel: SettingsViewModel,
    onLanguageChanged: (String) -> Unit,
    onEngineChanged: (String) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        logger.debug { "Launched SettingView" }
        viewModel.fetchSettings()
    }

    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {

        val settingsState by viewModel.uiState.collectAsState()
        //Language
        DropdownSettingsItem(
            viewModel.languages.values.toList(),
            settingsState.currentLanguage,
            stringResource(id = R.string.language),
            stringResource(id = R.string.language_desc),
            onLanguageChanged)
        //Engine
        DropdownSettingsItem(viewModel.engines,
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
        var selectedText by remember { mutableStateOf(selectedItem) }
        selectedText = selectedItem

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
                value = selectedText,
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
                            selectedText = item
                            expanded = false
                            onChanged(item)
                            Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
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

    init {
        fetchSettings()
    }

    fun fetchSettings() {
        logger.info { "Init SettingsViewModel" }

        viewModelScope.launch(Dispatchers.IO) {
            val settingsEntity = settingsRepository.load()
            logger.debug { "Current Settings: $settingsEntity" }

            _uiState.value = SettingsState(
               currentEngine = settingsEntity.engine.displayName,
                currentLanguage = languages.getValue(settingsEntity.language)
            )
        }

    }

    fun onLanguageChanged(lang: Languages) {
        logger.info { "Updating settings Language with: $lang" }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateLanguage(lang)
        }
    }

    fun onEngineChanged(engine: Engines) {
        logger.info { "Updating settings engine with $engine" }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.updateEngine(engine)
        }
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

//@Composable
//@Preview(showBackground = true)
//fun previewSettingsView() {
//
//    val availableLanguages = listOf(
//        "Deutsch", "Englisch")
//
//    val availableEngines = listOf("htmlunit+js", "htmlunit-js", "ktor+okhttp")
//    val context = LocalContext.current
//
//    val settingsRepository: SettingsRepository by lazy {
//        SettingsRepositoryImpl(SettingsDatabase.getDatabase(context).settingsDao)
//    }
//
//    SettingsView(
//        viewModel = SettingsViewModel(
//            languages = availableLanguages,
//            fetchEngines = availableEngines,
//            settingsRepository = settingsRepository))
//}