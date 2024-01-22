package de.dkutzer.tcgwatcher.views

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.products.adapter.PokemonPager
import de.dkutzer.tcgwatcher.products.adapter.api.BaseCardmarketApiClient
import de.dkutzer.tcgwatcher.products.adapter.api.CardmarketApiClientFactory
import de.dkutzer.tcgwatcher.products.adapter.api.DummyApiClient
import de.dkutzer.tcgwatcher.products.adapter.port.GetPokemonList
import de.dkutzer.tcgwatcher.products.config.CardmarketConfig
import de.dkutzer.tcgwatcher.products.domain.*
import de.dkutzer.tcgwatcher.products.domain.port.*
import de.dkutzer.tcgwatcher.products.services.CardmarketPokemonRepositoryImpl
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

private val logger = KotlinLogging.logger {}

@Composable
fun SearchActivity(
    snackbarHostState: SnackbarHostState
) {

    val context = LocalContext.current

    val settingsDatabase : SettingsDatabase by lazy {
        SettingsDatabase.getDatabase(context)
    }
    val searchCacheDatabase : SearchCacheDatabase by lazy {
        SearchCacheDatabase.getDatabase(context)
    }


    val searchViewModel = viewModel<SearchViewModel>(
        factory = SearchViewModel.Factory,
        extras = MutableCreationExtras().apply {
            set(SettingsDbIdKey, settingsDatabase)
            set(SearchCacheRepoIdKey, searchCacheDatabase)
        }
    )

    val pokemonPagingItems = searchViewModel.pokemonPagingDataFlow.collectAsLazyPagingItems()

    if (pokemonPagingItems.loadState.refresh is LoadState.Error) {
        LaunchedEffect(key1 = snackbarHostState) {
            snackbarHostState.showSnackbar(
                (pokemonPagingItems.loadState.refresh as LoadState.Error).error.message ?: ""
            )
        }
    }

    SearchView(
        pokemonPagingItems = pokemonPagingItems,
        onSearchQueryChange = { searchViewModel.onSearchQueryChange(it) },
        onSearchSubmit = { searchViewModel.onSearchSubmit(it) }
    )
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchView(
    pokemonPagingItems: LazyPagingItems<SearchProductModel>,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit
) {

    var query: String by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) } //needed to indicate if a searchResultItem is clickable

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        SearchBar(
            query = query,
            onQueryChange = { text ->
                logger.debug { "OnQueryChange: $text" }
                query = text
            },
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
                if (query.isNotEmpty()) {
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
                logger.debug { "onSearch: $it" }
                active = false
                onSearchSubmit(query.uppercase())
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
            CenterHorizontally
        ) {

            if (pokemonPagingItems.loadState.refresh is LoadState.Loading) {


                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                return
            }

            if (pokemonPagingItems.itemCount == 0) {
                NoSearchResults()
            } else {

                LazyColumn(
                    modifier = Modifier.weight(0.95f)
                ) {
                    items(
                        count = pokemonPagingItems.itemCount,
                        key = pokemonPagingItems.itemKey { it.id } ){ index ->
                        val productModel = pokemonPagingItems[index]
                        ItemOfInterestCard(
                            productModel = productModel as BaseProductModel,
                            showLastUpdated = false,
                            iconRowContent = { SearchViewCardIconRow() },
                        )
                    }
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


class SearchViewModel(
   private val settingsDatabase: SettingsDatabase,
    private val searchCacheDatabase: SearchCacheDatabase,
) : ViewModel() {


    private val _settings: MutableStateFlow<SettingsEntity> = MutableStateFlow(
        SettingsEntity(id = 1,
            language = Languages.EN,
            engine = Engines.KTOR )
    )
    private val settings: StateFlow<SettingsEntity> = _settings.asStateFlow()


    private val _query: MutableStateFlow<String> = MutableStateFlow("")
    private val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemonPagingDataFlow: Flow<PagingData<SearchProductModel>> =
        query.flatMapLatest { sq ->

            val config = CardmarketConfig(settings.value)

            val productApiClient = CardmarketApiClientFactory(config).create()
            val pokemonPager =
                PokemonPager.providePokemonPager(
                    sq,
                    searchCacheDatabase,
                    productApiClient
                )
            val pokemonRepositoryImpl = CardmarketPokemonRepositoryImpl(pokemonPager)

            val getPokemonList = GetPokemonList(pokemonRepositoryImpl)
            getPokemonList().cachedIn(viewModelScope)
        }

    init {
        logger.debug { "SearchViewModel init" }
        viewModelScope.launch(Dispatchers.IO) {
            _settings.value =  SettingsRepositoryImpl(settingsDatabase.settingsDao).load()
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
                logger.info { "Creating SearchViewModel" }
                val settingsRepo = extras[SettingsDbIdKey]
                val searchCacheRepository = extras[SearchCacheRepoIdKey]

                return SearchViewModel(
                    settingsRepo!!,
                    searchCacheRepository!!,
                ) as T
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        logger.debug { "onSearchQueryChange: $newQuery" }
        if(newQuery.isEmpty()) {
            logger.debug { "Empty search" }
            return
        }
        _query.value = newQuery
    }

    fun onSearchSubmit(searchString: String) {

        logger.debug { "onSearchSubmit: $searchString" }
        if(searchString.isEmpty()) {
            logger.debug { "Empty search" }
            return
        }
        _query.value = searchString

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
//
//@Preview(showBackground = true)
//@Composable
//fun TestSearchPreview() {
//
//  //  searchViewModel.searchResults = (Datasource().loadMockSearchData())
//    SearchActivity()
//
//}