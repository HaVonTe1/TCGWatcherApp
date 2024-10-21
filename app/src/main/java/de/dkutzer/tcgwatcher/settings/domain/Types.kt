package de.dkutzer.tcgwatcher.settings.domain

enum class Languages {
    DE, EN;
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


