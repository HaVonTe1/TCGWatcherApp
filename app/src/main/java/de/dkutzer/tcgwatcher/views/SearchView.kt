package de.dkutzer.tcgwatcher.views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.twotone.Add
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.products.adapter.ProductCardmarketRepositoryAdapter
import de.dkutzer.tcgwatcher.products.adapter.api.CardmarketApiClientImpl
import de.dkutzer.tcgwatcher.products.adapter.api.ProductApiClient
import de.dkutzer.tcgwatcher.products.adapter.port.ProductRepository
import de.dkutzer.tcgwatcher.products.config.CardmarketConfig
import de.dkutzer.tcgwatcher.products.domain.model.ProductModel
import de.dkutzer.tcgwatcher.products.services.ProductMapper
import de.dkutzer.tcgwatcher.products.services.ProductService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

val searchViewModel = SearchViewModel()

@Composable
fun SearchView() {


    val searchResults by searchViewModel.searchResults.collectAsStateWithLifecycle()

    SearchView(
        searchQuery = searchViewModel.searchQuery,
        searchResults = searchResults,
        onSearchQueryChange = { searchViewModel.onSearchQueryChange(it) },
        onSearchSubmit = { searchViewModel.onSearchSubmit(it) }
    )
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchView(
    searchQuery: String,
    searchResults: List<ProductModel>,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit
) {

    var active by rememberSaveable { mutableStateOf(false) } //needed to indicate if a searchResultItem is clickable

    Column(
        modifier = Modifier.fillMaxSize()
    ) {


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
            onSearch = {
                active = false
                onSearchSubmit(searchQuery)
            },
            active = active,
            onActiveChange = {
                active = it
            },
            tonalElevation = 4.dp,
            content = {
                //TODO:           add history items here
            }
        )
        if (searchResults.isEmpty()) {
            NoSearchResults()
        } else {

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(searchResults) {
                    ItemOfInterestCard(
                        productModel = it,
                        showLastUpdated = false,
                        iconRowContent = { SearchViewCardIconRow() },
                    )
                }
            }
        }
    }
}

@Composable
fun SearchViewCardIconRow( modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(1.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        ClickableIconButton(icon = Icons.TwoTone.Add, desc = stringResource(id = R.string.addDesc), onClick = {})
    }
}



class SearchViewModel : ViewModel() {

    //todo: look for a DI lib
    val cardmarketConfig = CardmarketConfig()
    val productApiClient = CardmarketApiClientImpl(cardmarketConfig)
    val productRepository = ProductCardmarketRepositoryAdapter(productApiClient)
    val productMapper = ProductMapper(cardmarketConfig)
    val productService: ProductService = ProductService(productRepository, productMapper)

    var searchResults = createStateFlowFromItemList(mutableListOf())
    var searchQuery by mutableStateOf("")


    fun createStateFlowFromItemList(items: MutableList<ProductModel>):StateFlow<MutableList<ProductModel>> {
        val itemFlow = flowOf(
            items
        )


        return snapshotFlow { searchQuery }
            .combine(itemFlow) { searchQuery, items ->

                when {
                    searchQuery.isNotEmpty() -> items

                    else -> items
                }
            }.stateIn(
                scope = viewModelScope,
                initialValue = mutableListOf(),
                started = SharingStarted.WhileSubscribed(5_000)
            )


    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }
    fun onSearchSubmit(searchString: String) {
        searchResults.value.clear()
        searchResults.value.addAll(productService.search(searchString))
    }

}

@Composable
private fun NoSearchResults() {

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