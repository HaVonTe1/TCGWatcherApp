package de.dkutzer.tcgwatcher.settings.presentation.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose.TCGWatcherTheme
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DropdownSettingsItem(
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
        val expanded = remember(false) { mutableStateOf(false) }
        val selectedText = remember(selectedItem) { mutableStateOf(selectedItem) }


        Column(
            modifier = Modifier.weight(0.3f)
        ) {
            Text(
                modifier = Modifier
                    .padding(2.dp),
                text = "$label: ",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 20.sp

            )
            Text(
                modifier = Modifier
                    .padding(2.dp),
                text = "$labelDesc: ",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 10.sp

            )
        }

        ExposedDropdownMenuBox(
            modifier = Modifier
                .weight(0.7f)
                .padding(2.dp),
            expanded = expanded.value,
            onExpandedChange = {
                expanded.value = !expanded.value
            }
        ) {
            TextField(
                value = selectedText.value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
            )
            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            logger.debug { "DropDown OnClick: $item" }
                            selectedText.value = item
                            expanded.value = false
                            onChanged(item)
                            Toast.makeText(context, "$label changed to: $item", Toast.LENGTH_SHORT)
                                .show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
fun DropDonSettingItemPreview(modifier: Modifier = Modifier) {

    TCGWatcherTheme {
        val items = listOf("Item 1", "Item 2", "Item 3")

        DropdownSettingsItem(
            items = items,
            selectedItem = "Item 1",
            label = "Label",
            labelDesc = "LabelDesc",
            onChanged = {}
        )
    }


}