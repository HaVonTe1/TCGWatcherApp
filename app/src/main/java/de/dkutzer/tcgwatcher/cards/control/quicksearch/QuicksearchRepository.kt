package de.dkutzer.tcgwatcher.cards.control.quicksearch

import de.dkutzer.tcgwatcher.cards.entity.PokemonCardQuickEntity
import kotlin.math.log


interface QuicksearchRepository {

    fun find(query: String): List<PokemonCardQuickEntity>


}

class QuicksearchRepositoryImpl(private val dao: QuicksearchDao) : QuicksearchRepository {
    override fun find(query: String): List<PokemonCardQuickEntity> {
        val cardQuickEntities =
            dao.search(sanatizeQuery(query))
                .sortedByDescending { result -> calculateScore(result.matchInfo) }
                .map { result -> result.pokemonCardQuickEntity }
                .toList()

        return cardQuickEntities
    }

    private fun sanatizeQuery(query: String?): String {
        if (query == null) {
            return "";
        }
        val queryWithEscapedQuotes = query.replace(Regex.fromLiteral("\""), "\"\"")
        return "*\"$queryWithEscapedQuotes\"*"
    }

    //shamefully _inspired_ from:
    //https://github.com/CrisisCleanup/crisiscleanup-android/blob/main/core/database/src/main/java/com/crisiscleanup/core/database/dao/fts/IncidentFts.kt
    private fun calculateScore(matchInfo: ByteArray): Double {
        val info = matchInfo.toIntArray()

        val score = info.okapiBm25Score(0) * 3 +
                info.okapiBm25Score(1) * 2 +
                info.okapiBm25Score(2)

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

//shamefully _inspired_ from:
//https://github.com/CrisisCleanup/crisiscleanup-android/blob/main/core/database/src/main/java/com/crisiscleanup/core/database/util/FtsUtil.kt
//hopefully Google will add FTS5 to their ROOM library in the near future so I can use the build in ranking functions of it.
fun IntArray.okapiBm25Score(
    column: Int,
    b: Double = 0.75,
    k1: Double = 1.2,
): Double {
    val pOffset = 0
    val cOffset = 1
    val nOffset = 2
    val aOffset = 3

    val termCount = this[pOffset]
    val colCount = this[cOffset]

    val lOffset = aOffset + colCount
    val xOffset = lOffset + colCount

    val totalDocs = this[nOffset].toDouble()
    val avgLength = this[aOffset + column].toDouble()
    val docLength = this[lOffset + column].toDouble()

    var score = 0.0

    for (i in 0 until termCount) {
        val currentX = xOffset + (3 * (column + i * colCount))
        val termFrequency = this[currentX].toDouble()
        val docsWithTerm = this[currentX + 2].toDouble()

        val p = totalDocs - docsWithTerm + 0.5
        val q = docsWithTerm + 0.5
        val idf = log(p, q)

        val r = termFrequency * (k1 + 1)
        val s = b * (docLength / avgLength)
        val t = termFrequency + (k1 * (1 - b + s))
        val rightSide = r / t

        score += (idf * rightSide)
    }

    return score
}
