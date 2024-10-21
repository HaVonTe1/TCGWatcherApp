package de.dkutzer.tcgwatcher.collectables.search.presentation

import androidx.lifecycle.viewmodel.CreationExtras
import de.dkutzer.tcgwatcher.collectables.history.data.SearchCacheDatabase
import de.dkutzer.tcgwatcher.collectables.quicksearch.data.QuickSearchDatabase

class SearchModelCreationKeys {
    object SearchCacheRepoIdKey : CreationExtras.Key<SearchCacheDatabase>
    object QuickSearchRepoIdKey : CreationExtras.Key<QuickSearchDatabase>

}