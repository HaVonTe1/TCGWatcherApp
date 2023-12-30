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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import kotlinx.coroutines.launch
private val logger = KotlinLogging.logger {}


@Composable
fun SettingsActivity() {

    val scope = rememberCoroutineScope()
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
            scope.launch(Dispatchers.IO) {
                val key =
                    availableLanguages.entries.find { entry -> entry.value.compareTo(it) == 0 }?.key
                settingsViewModel.onLanguageChanged(key!!)
            }
        },
        onEngineChanged = {
            scope.launch(Dispatchers.IO) {
                val key = Engines.fromDisplayName(it)
                settingsViewModel.onEngineChanged(key!!)
            }
        }
    )
}

@Composable
fun SettingsView(
    viewModel: SettingsViewModel,
    onLanguageChanged: (String) -> Unit,
    onEngineChanged: (String) -> Unit
) {
    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {

        //Language
        DropdownSettingsItem(
            viewModel.languages.values.toList(),
            stringResource(id = R.string.language),
            stringResource(id = R.string.language_desc),
            onLanguageChanged)
        //Engine
        DropdownSettingsItem(viewModel.fetchEngines,
            stringResource(id = R.string.engine),
            stringResource(id = R.string.engine_desc),
            onEngineChanged)

    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DropdownSettingsItem(
    items: List<String>,
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
        var selectedText by remember { mutableStateOf(items[0]) }

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
                            onChanged(selectedText)
                            Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

class SettingsViewModel(
    val languages: Map<Languages,String>,
    val fetchEngines: List<String>,
    private val settingsRepository: SettingsRepository
): ViewModel() {


    var language by mutableStateOf(languages.getValue(Languages.DE))
        private set
    var engine by mutableStateOf(fetchEngines[0])
        private set

    suspend fun onLanguageChanged(lang: Languages) {

        logger.info { lang }
        settingsRepository.updateLanguage(lang)
    }

    suspend fun onEngineChanged(engine: Engines) {
        logger.info { engine }
        settingsRepository.updateEngine(engine)
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