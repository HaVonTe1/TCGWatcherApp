package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.dkutzer.tcgwatcher.collectables.search.domain.Condition
import de.dkutzer.tcgwatcher.collectables.search.domain.OfferFilters
import de.dkutzer.tcgwatcher.collectables.search.domain.SortField
import de.dkutzer.tcgwatcher.collectables.search.domain.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    initialFilters: OfferFilters,
    availableCountries: List<String>,
    availableLanguages: List<String>,
    onFiltersApplied: (OfferFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var filters by remember { mutableStateOf(initialFilters) }
    val allConditions = remember {
       Condition.entries.toList()
    }
    var priceRange by remember {
        mutableStateOf(initialFilters.priceRange)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter & Sort") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
//                // Seller Name Filter
//                OutlinedTextField(
//                    value = filters.sellerName,
//                    onValueChange = { filters = filters.copy(sellerName = it) },
//                    label = { Text("Seller Name") },
//                    modifier = Modifier.fillMaxWidth()
//                )

                // Country Filter
                FilterSection("Countries") {
                    availableCountries.forEach { country ->
                        FilterChip(
                            selected = country in filters.sellerCountries,
                            onClick = {
                                filters = filters.copy(
                                    sellerCountries = filters.sellerCountries.toMutableSet().apply {
                                        if (contains(country)) remove(country) else add(country)
                                    }
                                )
                            },
                            label = { Text(country) }
                        )
                    }
                }

                // Language Filter
                FilterSection("Languages") {
                    availableLanguages.forEach { language ->
                        FilterChip(
                            selected = language in filters.languages,
                            onClick = {
                                filters = filters.copy(
                                    languages = filters.languages.toMutableSet().apply {
                                        if (contains(language)) remove(language) else add(language)
                                    }
                                )
                            },
                            label = { Text(language) }
                        )
                    }
                }

                // Condition Filter
                FilterSection("Conditions") {
                    allConditions.forEach { condition ->
                        FilterChip(
                            selected = condition in filters.conditions,
                            onClick = {
                                filters = filters.copy(
                                    conditions = filters.conditions.toMutableSet().apply {
                                        if (contains(condition)) remove(condition) else add(condition)
                                    }
                                )
                            },
                            label = { Text(condition.displayName) }
                        )
                    }
                }
//
//                // Price Range
//                FilterSection("Price Range") {
//                    var min by remember { mutableStateOf(priceRange.start.toString()) }
//                    var max by remember { mutableStateOf(priceRange.endInclusive.toString()) }
//
//                    Column {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            OutlinedTextField(
//                                value = min,
//                                onValueChange = { min = it },
//                                label = { Text("Min") },
//                                modifier = Modifier.weight(1f),
//                                keyboardOptions = KeyboardOptions.Default.copy(
//                                    keyboardType = KeyboardType.Number
//                                )
//                            )
//                            Spacer(Modifier.width(8.dp))
//                            OutlinedTextField(
//                                value = max,
//                                onValueChange = { max = it },
//                                label = { Text("Max") },
//                                modifier = Modifier.weight(1f),
//                                keyboardOptions = KeyboardOptions.Default.copy(
//                                    keyboardType = KeyboardType.Number
//                                )
//                            )
//                        }
//
//                        RangeSlider(
//                            value = priceRange,
//                            onValueChange = { priceRange = it },
//                            valueRange = 0f..1000f,
//                            steps = 99,
//                            modifier = Modifier.padding(vertical = 8.dp)
//                        )
//                    }
//                }

                // Sorting
                FilterSection("Sort By") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = {}
                        ) {
                            TextField(
                                readOnly = true,
                                value = filters.sortBy.name.replace("_", " "),
                                onValueChange = {},
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = false
                                    )
                                },
                                modifier = Modifier.menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = false,
                                onDismissRequest = {}
                            ) {
                                SortField.values().forEach { field ->
                                    DropdownMenuItem(
                                        text = { Text(field.name.replace("_", " ")) },
                                        onClick = { filters = filters.copy(sortBy = field) }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Row {
                            RadioButton(
                                selected = filters.sortOrder == SortOrder.ASCENDING,
                                onClick = { filters = filters.copy(sortOrder = SortOrder.ASCENDING) }
                            )
                            Text("Ascending")

                            Spacer(Modifier.width(8.dp))

                            RadioButton(
                                selected = filters.sortOrder == SortOrder.DESCENDING,
                                onClick = { filters = filters.copy(sortOrder = SortOrder.DESCENDING) }
                            )
                            Text("Descending")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    filters = filters.copy(priceRange = priceRange)
                    onFiltersApplied(filters)
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { content() }
        }
    }
}