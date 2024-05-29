package de.dkutzer.tcgwatcher.cards.control.quicksearch

import de.dkutzer.tcgwatcher.cards.entity.PokemonCardQuickEntity


interface QuickSearchRepository {

    suspend fun find(query: String): List<PokemonCardQuickEntity>


}

class QuickSearchRepositoryImpl(private val dao: QuickSearchDao) : QuickSearchRepository {
    override suspend fun find(query: String): List<PokemonCardQuickEntity> {
        val cardQuickEntities =
            dao.search(sanatizeQuery(query))
                .sortedByDescending { result -> calculateScore(result.matchInfo) }
                .map { result -> result.pokemonCardQuickEntity }
                .toList()

        return cardQuickEntities
    }

    private fun sanatizeQuery(query: String?): String {
        if (query == null) {
            return ""
        }
        val queryWithEscapedQuotes = query.replace(Regex.fromLiteral("\""), "\"\"")
        val tokenizedQuery = queryWithEscapedQuotes.trim().split(" ").joinToString(" ") { word -> "*$word*" }
        return tokenizedQuery
    }

    private fun calculateScore(matchInfo: ByteArray): Double {
        val info = matchInfo.toIntArray()
        val score = rank(info)
        return score
    }


}

fun ByteArray.toIntArray(skipSize: Int = 4): IntArray {
    val cleanedArr = IntArray(this.size / skipSize)
    for ((pointer, i) in (this.indices step skipSize).withIndex()) {
        cleanedArr[pointer] = this[i].toInt()
    }

    return cleanedArr
}


//https://stackoverflow.com/questions/63654824/how-to-fix-the-error-wrong-number-of-arguments-to-function-rank-on-sqlite-an
fun rank(matchInfo: IntArray): Double {
    val numPhrases = matchInfo[0]
    val numColumns = matchInfo[1]

    var score = 0.0
    for (phrase in 0 until numPhrases) {
        val offset = 2 + phrase * numColumns * 3
        for (column in 0 until numColumns) {
            val numHitsInRow = matchInfo[offset + 3 * column]
            val numHitsInAllRows = matchInfo[offset + 3 * column + 1]
            if (numHitsInAllRows > 0) {
                score += numHitsInRow.toDouble() / numHitsInAllRows.toDouble()
            }
        }
    }

    return score
}
