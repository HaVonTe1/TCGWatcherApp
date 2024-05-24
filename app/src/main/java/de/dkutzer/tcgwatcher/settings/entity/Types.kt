package de.dkutzer.tcgwatcher.settings.entity

import androidx.lifecycle.viewmodel.CreationExtras
import de.dkutzer.tcgwatcher.cards.control.SearchCacheDatabase
import de.dkutzer.tcgwatcher.settings.control.SettingsDatabase
import de.dkutzer.tcgwatcher.settings.control.SettingsRepository

enum class Languages(val code: String) {
    DE("de"), EN("en");
}


enum class Engines(val displayName: String) {
    HTMLUNIT_JS("htmlunit+js"),
    HTMLUNIT_NOJS("htmlunit-without-js"),
    KTOR("ktor+okhttp"),
    TESTING("testing");

    companion object {
        private val map = entries.associateBy(Engines::displayName)
        fun fromDisplayName(dn: String) = map[dn]
    }
}

object LanguagesIdKey : CreationExtras.Key<Map<Languages, String>>
object EnginesIdKey : CreationExtras.Key<List<String>>
object SettingsRepoIdKey : CreationExtras.Key<SettingsRepository>
object SettingsDbIdKey : CreationExtras.Key<SettingsDatabase>
object SearchCacheRepoIdKey : CreationExtras.Key<SearchCacheDatabase>
