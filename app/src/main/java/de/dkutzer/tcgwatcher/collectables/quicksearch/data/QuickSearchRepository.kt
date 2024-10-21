package de.dkutzer.tcgwatcher.collectables.quicksearch.data

import de.dkutzer.tcgwatcher.collectables.quicksearch.domain.PokemonCardQuickEntityWithMatchInfo
import de.dkutzer.tcgwatcher.collectables.quicksearch.domain.PokemonCardQuickNormalizedEntity
import de.dkutzer.tcgwatcher.collectables.quicksearch.domain.QuickSearchRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.measureTimeMillis


private val logger = KotlinLogging.logger {}


class QuickSearchRepositoryImpl(private val dao: QuickSearchDao) : QuickSearchRepository {

    override suspend fun find(query: String): List<PokemonCardQuickNormalizedEntity> {
        var cardQuickEntities = emptyList<PokemonCardQuickNormalizedEntity>()
        val duration = measureTimeMillis {
            var pokemonCardQuickEntityWithMatchInfos: List<PokemonCardQuickEntityWithMatchInfo> =
                emptyList()
            val innerDuration = measureTimeMillis {

                val sanatizeQueryWrapper = sanatizeQuery(query)
                logger.debug { "Sanitized query: $sanatizeQueryWrapper" }

                if (!sanatizeQueryWrapper.isEmpty()) {
                    pokemonCardQuickEntityWithMatchInfos =
                        if (sanatizeQueryWrapper.code.isEmpty()) {
                            logger.debug { "Full text search with names: ${sanatizeQueryWrapper.names}" }
                            dao.fullTextSearchOverAllColumns(sanatizeQueryWrapper.names)
                        } else {
                            logger.debug { "Full text search with names: ${sanatizeQueryWrapper.names} and code: ${sanatizeQueryWrapper.code}" }

                            dao.fullTextSearchWithCode(
                                sanatizeQueryWrapper.names,
                                sanatizeQueryWrapper.code
                            )
                        }
                }
            }
            logger.debug { "Inner query executed in $innerDuration ms" }
            cardQuickEntities =
                pokemonCardQuickEntityWithMatchInfos
                    .sortedByDescending { result -> calculateScore(result.matchInfo) }
                    .map { result -> result.pokemonCardQuickEntity }
                    .toList()

        }
        logger.debug { "Query executed in $duration ms" }

        return cardQuickEntities
    }

    fun sanatizeQuery(query: String?): QueryWrapper {
        var result = QueryWrapper.empty()


        val duration = measureTimeMillis {
            if (query == null) {
                return result
            }
            if (query.trim().count() < 4) {
                //a query with less then 4 characters is not valid because its to slow to search
                return result
            }
            val queryList = query.trim().split(" ").toMutableList()
            var queryWithDigts = queryList.find { s -> s.any { it in "0123456789" } }
            queryList.remove(queryWithDigts)
            queryWithDigts = if (queryWithDigts == null) {
                ""
            } else {
                "%${queryWithDigts.lowercase()}%"
            }
            if (queryList.count() > 1) {
                //Remove all elements in the list that have less then 3 characters and doesnt contains any digits because this would mean a search by number
                queryList.removeAll { queryString -> queryString.length < 4 }
            }
            val moddedQuery = queryList.joinToString(" ") { word -> "*${word.lowercase()}*" }
                .replace(Regex.fromLiteral("\""), "\"\"")
            result = QueryWrapper(names = moddedQuery, code = queryWithDigts)

        }
        logger.debug { "Query sanitized in $duration ms" }
        return result
    }

    private fun calculateScore(matchInfo: ByteArray): Double = rank(matchInfo.toIntArray())

}


data class QueryWrapper(val names: String, val code: String) {
    companion object {
        fun empty(): QueryWrapper = QueryWrapper(names = "", code = "")
    }

    fun isEmpty() = names.isEmpty() && code.isEmpty()
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
