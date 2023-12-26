package de.dkutzer.tcgwatcher.products.domain

enum class Languages(val code: String) {
    DE("de"), EN("en")
}

enum class Engines(val displayName: String) {
    HTMLUNIT_JS("htmlunit+js"),
    HTMLUNIT_NOJS("htmlunit-without-js"),
    KTOR("ktor+okhttp")
}