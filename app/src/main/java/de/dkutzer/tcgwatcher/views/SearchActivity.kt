package de.dkutzer.tcgwatcher.views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.KeyboardArrowLeft
import androidx.compose.material.icons.twotone.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import de.dkutzer.tcgwatcher.Datasource
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.products.adapter.ProductCardmarketRepositoryAdapter
import de.dkutzer.tcgwatcher.products.adapter.api.CardmarketHtmlUnitApiClientImpl
import de.dkutzer.tcgwatcher.products.config.CardmarketConfig
import de.dkutzer.tcgwatcher.products.services.ProductMapper
import de.dkutzer.tcgwatcher.products.services.SearchProductModel
import de.dkutzer.tcgwatcher.products.services.ProductService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


@Composable
fun SearchActivity() {


    val scope = rememberCoroutineScope()

    val searchViewModel = viewModel<SearchViewModel>()

    SearchView(
        searchViewModel = searchViewModel,
        onSearchQueryChange = { searchViewModel.onSearchQueryChange(it) },
        onSearchSubmit = {
            scope.launch(Dispatchers.IO) {
                searchViewModel.onSearchSubmit(it)
            }
        },
        onForward = { scope.launch(Dispatchers.IO) { searchViewModel.onForward() } },
        onBackward = { scope.launch(Dispatchers.IO) { searchViewModel.onBackward() } }
    )
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchView(
    searchViewModel: SearchViewModel,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onForward: () -> Unit,
    onBackward: () -> Unit
) {


    var active by rememberSaveable { mutableStateOf(false) } //needed to indicate if a searchResultItem is clickable

    Column(
        modifier = Modifier.fillMaxSize()
    ) {


        SearchBar(
            query = searchViewModel.searchQuery,
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
                if (searchViewModel.searchQuery.isNotEmpty()) {
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
                onSearchSubmit(searchViewModel.searchQuery)
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
        Column(
            modifier = Modifier.fillMaxSize(),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {

            if(searchViewModel.searching) {


                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                return
            }



            if (searchViewModel.searchResults.isEmpty()) {
                NoSearchResults()
            } else {

                LazyColumn(
                    modifier = Modifier.weight(0.95f)
                ) {
                    items(searchViewModel.searchResults.size) {
                        val productModel = searchViewModel.searchResults[it]
                        ItemOfInterestCard(
                            productModel = productModel,
                            showLastUpdated = false,
                            iconRowContent = { SearchViewCardIconRow() },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(1.dp)
                        .weight(0.05f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically


                ) {

                    Spacer(modifier = Modifier.weight(0.5f))
                    ClickableIconButton(
                        modifier = Modifier.weight(0.1f),
                        icon = Icons.TwoTone.KeyboardArrowLeft,
                        desc = stringResource(id = R.string.back),
                        enabled = searchViewModel.currentPage != 1,
                        onClick = {
                            onBackward()
                        }
                    )
                    Text(
                        modifier = Modifier.padding(1.dp),
                        text = stringResource(id = R.string.pageXofY).format(
                            searchViewModel.currentPage,
                            searchViewModel.totalPages
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )

                    ClickableIconButton(
                        modifier = Modifier.weight(0.1f),
                        icon = Icons.TwoTone.KeyboardArrowRight,
                        desc = stringResource(id = R.string.forward),
                        enabled = searchViewModel.currentPage != searchViewModel.totalPages,
                        onClick = {
                            onForward()
                        })
                    Spacer(modifier = Modifier.weight(0.5f))

                }
            }

        }

    }
}

@Composable
fun SearchViewCardIconRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(1.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        ClickableIconButton(
            icon = Icons.TwoTone.Add,
            desc = stringResource(id = R.string.addDesc),
            onClick = {})
    }
}


class SearchViewModel : ViewModel() {

    //todo: look for a DI lib ... Dagger ..
    private val cardmarketConfig = CardmarketConfig()
    private val productApiClient = CardmarketHtmlUnitApiClientImpl(cardmarketConfig)
    private val productRepository = ProductCardmarketRepositoryAdapter(productApiClient)
    private val productMapper = ProductMapper(cardmarketConfig)
    private val productService: ProductService = ProductService(productRepository, productMapper)

    var searchResults by mutableStateOf(listOf<SearchProductModel>())
        private set
    var searchQuery by mutableStateOf("")
        private set
    var currentPage by mutableIntStateOf(1)
        private set
    var totalPages by mutableIntStateOf(1)
        private set

    var searching by mutableStateOf(false)



    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    suspend fun onSearchSubmit(searchString: String) {
        currentPage = 1
        updateSearchResultsWithNewSearch(searchString, 1)
    }

    private suspend fun updateSearchResultsWithNewSearch(searchString: String, page: Int) {
        searching = true
        val searchItemModelList = productService.search(searchString, page)
        searchResults = searchItemModelList.products
        totalPages = searchItemModelList.pages
        searching = false
    }

    suspend fun onForward() {
        currentPage++
        updateSearchResultsWithNewSearch(searchQuery, currentPage)

    }

    suspend fun onBackward() {
        currentPage--
        updateSearchResultsWithNewSearch(searchQuery, currentPage)
    }



}

@Composable
private fun NoSearchResults() {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        Text(stringResource(id = R.string.emptySearch))
    }
}

@Preview(showBackground = true)
@Composable
fun TestSearchPreview() {

  //  searchViewModel.searchResults = (Datasource().loadMockSearchData())
    SearchActivity()

}