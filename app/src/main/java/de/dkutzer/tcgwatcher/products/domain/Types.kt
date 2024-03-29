package de.dkutzer.tcgwatcher.products.domain

import androidx.lifecycle.viewmodel.CreationExtras
import de.dkutzer.tcgwatcher.products.adapter.port.GetPokemonList
import de.dkutzer.tcgwatcher.products.domain.port.SearchCacheDatabase
import de.dkutzer.tcgwatcher.products.domain.port.SettingsDatabase
import de.dkutzer.tcgwatcher.products.domain.port.SettingsRepository

enum class Languages(val code: String) {
    DE("de"), EN("en");


}


enum class Engines(val displayName: String) {
    HTMLUNIT_JS("htmlunit+js"),
    HTMLUNIT_NOJS("htmlunit-without-js"),
    KTOR("ktor+okhttp");

    companion object {
        private val map = Engines.values().associateBy(Engines::displayName)
        fun fromDisplayName(dn: String) = map[dn]
    }
}

object LanguagesIdKey : CreationExtras.Key<Map<Languages, String>>
object EnginesIdKey : CreationExtras.Key<List<String>>
object SettingsRepoIdKey : CreationExtras.Key<SettingsRepository>
object SettingsDbIdKey : CreationExtras.Key<SettingsDatabase>
object SearchCacheRepoIdKey : CreationExtras.Key<SearchCacheDatabase>
object PokemonPagingIdKey : CreationExtras.Key<GetPokemonList>