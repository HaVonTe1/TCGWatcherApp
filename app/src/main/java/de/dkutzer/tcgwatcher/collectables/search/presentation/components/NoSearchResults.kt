package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import de.dkutzer.tcgwatcher.R
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme

@Composable
fun NoSearchResults() {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(
            count = 1
        ) {

            Text(stringResource(id = R.string.emptySearch))
        }
    }
}

@PreviewLightDark
@Composable
fun NoSearchResultsPreview(modifier: Modifier = Modifier) {
    TCGWatcherTheme {
        NoSearchResults()

    }
}