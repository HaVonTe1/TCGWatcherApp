package de.dkutzer.tcgwatcher.views

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import de.dkutzer.tcgwatcher.R

object LanguagesIdKey : CreationExtras.Key<List<String>>
object EnginesIdKey : CreationExtras.Key<List<String>>
@Composable
fun SettingsActivity() {

    val scope = rememberCoroutineScope()
    val availableLanguages = listOf(
        stringResource(id = R.string.german), stringResource(id = R.string.english))

    val availableEngines = listOf("htmlunit with js", "htmlunit NO js", "ktor+okhttp")


    val settingsViewModel = viewModel<SettingsViewModel>(
        factory = SettingsViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(LanguagesIdKey, availableLanguages)
            set(EnginesIdKey, availableEngines)
        }
    )


    SettingsView(settingsViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    viewModel: SettingsViewModel
) {
    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {

        //Language
        dropdownSettingsItem(viewModel.languages, stringResource(id = R.string.language), stringResource(
            id = R.string.language_desc
        ))
        dropdownSettingsItem(viewModel.fetchEngines, stringResource(id = R.string.engine), stringResource(
            id = R.string.engine_desc
        ))

        //Engine

    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun dropdownSettingsItem(items: List<String>, label: String, label_desc: String) {
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
                text = "$label_desc: ",
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
                            Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

class SettingsViewModel(
    val languages: List<String>,
    val fetchEngines: List<String>
): ViewModel() {

    var language by mutableStateOf(languages[0])
        private set
    var engine by mutableStateOf("htmlunit+js")
        private set


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
                return SettingsViewModel(
                    languages!!, engines!!
                ) as T
            }
        }
    }

}

@Composable
@Preview(showBackground = true)
fun previewSettingsView() {

    val availableLanguages = listOf(
        "Deutsch", "Englisch")

    val availableEngines = listOf("htmlunit+js", "htmlunit-js", "ktor+okhttp")


    SettingsView(viewModel = SettingsViewModel(languages = availableLanguages, fetchEngines = availableEngines))
}