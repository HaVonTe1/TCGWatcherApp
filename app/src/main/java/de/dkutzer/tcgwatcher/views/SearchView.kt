package de.dkutzer.tcgwatcher.views

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import de.dkutzer.tcgwatcher.Datasource
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.models.ItemOfInterest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

val searchViewModel = SearchViewModel()
fun onSearchQueryChange(newQuery: String) {
    searchQuery = newQuery

}

@Composable
fun SearchView() {
    val searchResults by searchViewModel.searchResults.collectAsStateWithLifecycle()

    SearchView(
        searchQuery = searchQuery,
        searchResults = searchResults,
        onSearchQueryChange = { onSearchQueryChange(it) }
    )
}
var searchQuery by mutableStateOf("")

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchView(
    searchQuery: String,
    searchResults: List<ItemOfInterest>,
    onSearchQueryChange: (String) -> Unit
) {

    var active by rememberSaveable { mutableStateOf(false) } //needed to indicate if a searchResultItem is clickable
    SearchBar(
        query = searchQuery,
        onQueryChange = onSearchQueryChange,

        placeholder = {
            Text(text = stringResource(id = R.string.searchPlaceHolder))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = stringResource(id = R.string.clearSearch)
                    )
                }
            }
        },
        onSearch = { active = false },
        active = active,
        onActiveChange = {
            active = it
        },
        tonalElevation = 4.dp,
        content = {
//           add history items here
        }
    )
    if(searchResults.isEmpty()) {
        NoSearchResults()
    }else
    {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                count = searchResults.size,
                key = { index -> searchResults[index].id },
                itemContent = { index ->
                    val item = searchResults[index]
                    SearchListItem(item = item)
                }
            )
        }
    }
    }


@Composable
fun SearchListItem(
    item: ItemOfInterest,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth().border(1.dp, Color.Black)
    ) {
        Text(text = item.details.name)
        Text(text = item.details.price.toString())
    }
}


class SearchViewModel : ViewModel() {

    var searchResults = createStateFlowFromItemList(emptyList())

    fun createStateFlowFromItemList(items: List<ItemOfInterest>):StateFlow<List<ItemOfInterest>> {
        val itemFlow = flowOf(
            items
        )
        return snapshotFlow { searchQuery }
            .combine(itemFlow) { searchQuery, items ->

                when {
                    searchQuery.isNotEmpty() -> items.filter { item ->
                        item.details.name.contains(searchQuery, ignoreCase = true)
                    }

                    else -> items
                }
            }.stateIn(
                scope = viewModelScope,
                initialValue = emptyList(),
                started = SharingStarted.WhileSubscribed(5_000)
            )

    }

}

@Composable
fun NoSearchResults() {

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        Text(stringResource(id = R.string.emptySearch))
    }
}
@Preview(showBackground = true)
@Composable
fun TestSearchPreview() {
    SearchView()

}