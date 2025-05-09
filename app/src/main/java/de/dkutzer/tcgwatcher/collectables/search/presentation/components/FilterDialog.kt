package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.derivedStateOf
import de.dkutzer.tcgwatcher.collectables.search.domain.Condition
import de.dkutzer.tcgwatcher.collectables.search.domain.OfferFilters
import de.dkutzer.tcgwatcher.collectables.search.domain.SortField
import de.dkutzer.tcgwatcher.collectables.search.domain.SortOrder

@SuppressLint("UnrememberedMutableState")
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
                                        if (contains(condition)) remove(condition) else add(
                                            condition
                                        )
                                    }
                                )
                            },
                            label = { Text(condition.displayName) }
                        )
                    }
                }

                // Price range handling with validation
                val (priceInput, setPriceInput) = remember {
                    mutableStateOf(
                        "${initialFilters.priceRange.start}" to "${initialFilters.priceRange.endInclusive}"
                    )
                }

                val priceRange by derivedStateOf {
                    val min = priceInput.first.toFloatOrNull() ?: 0f
                    val max = priceInput.second.toFloatOrNull() ?: 1000f
                    min..max
                }


                // Improved Price Range Section
                FilterSection("Price Range") {
                    Column {
                        // Input fields
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = priceInput.first,
                                onValueChange = { setPriceInput(it to priceInput.second) },
                                label = { Text("Min Price") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true
                            )

                            Spacer(Modifier.width(8.dp))

                            OutlinedTextField(
                                value = priceInput.second,
                                onValueChange = { setPriceInput(priceInput.first to it) },
                                label = { Text("Max Price") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true
                            )
                        }

                        // Slider with proper bounds
                        RangeSlider(
                            value = priceRange,
                            onValueChange = {
                                setPriceInput(
                                    "${it.start}".take(6) to
                                            "${it.endInclusive}".take(6))
                            },
                            valueRange = 0f..1000f,
                            steps = 100,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                FilterSection("Sort By") {
                    var sortingExpanded by remember { mutableStateOf(false) }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExposedDropdownMenuBox(
                            expanded = sortingExpanded,
                            onExpandedChange = { sortingExpanded = it }
                        ) {
                            TextField(
                                readOnly = true,
                                value = filters.sortBy.name.replace("_", " "),
                                onValueChange = {},
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = sortingExpanded
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(0.6f)
                            )

                            ExposedDropdownMenu(
                                expanded = sortingExpanded,
                                onDismissRequest = { sortingExpanded = false }
                            ) {
                                SortField.values().forEach { field ->
                                    DropdownMenuItem(
                                        text = { Text(field.name.replace("_", " ")) },
                                        onClick = {
                                            filters = filters.copy(sortBy = field)
                                            sortingExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        // New icon buttons for sort direction
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            IconButton(
                                onClick = { filters = filters.copy(sortOrder = SortOrder.ASCENDING) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Sort ascending",
                                    tint = if (filters.sortOrder == SortOrder.ASCENDING) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }

                            Spacer(Modifier.width(4.dp))

                            IconButton(
                                onClick = { filters = filters.copy(sortOrder = SortOrder.DESCENDING) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Sort descending",
                                    tint = if (filters.sortOrder == SortOrder.DESCENDING) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
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
// Improved FilterSection with wrapping
@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}