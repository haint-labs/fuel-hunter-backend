package fuel.hunter.scrapers.internal

import fuel.hunter.Snapshot
import fuel.hunter.extensions.snapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

private const val providerName = "Circle K"

private val priceRegex = "(.+)\\s(.+)".toRegex()

private val fuelTypeMap = mapOf(
    "1335453340249" to "95 miles",
    "1335520335162" to "98 miles PLUS",
    "1335453340279" to "D miles",
    "1335520335169" to "D miles PLUS",
    "1335453340203" to "gas"
)

class CircleKScrapper : Scraper {
    override fun scrape(document: Document): Flow<Snapshot> = flow {
        document.circlekSnapshotChunks.forEach { chunk ->
            val fuelType = getFuelType(chunk[0].html())

            val (rawPrice) = priceRegex
                .matchEntire(chunk[1].text())
                ?.destructured
                ?: throw IllegalArgumentException("Invalid price")

            chunk[2].text()
                .split(", ")
                .forEach { raw ->
                    val composedName = raw.streetName
                        .takeUnless { it.isEmpty() }
                        ?.let { "$providerName, $it" }
                        ?: providerName

                    val item = snapshot {
                        provider = providerName
                        name = composedName
                        address = raw
                        city = "Rīga"
                        type = fuelType
                        price = rawPrice.toFloat()
                    }

                    emit(item)
                }
        }
    }

    private fun getFuelType(rawImageTag: String): String {
        val key = fuelTypeMap.keys.firstOrNull { rawImageTag.contains(it) }
            ?: throw IllegalArgumentException("Unknown type provided")

        return fuelTypeMap[key]
            ?: throw IllegalArgumentException("Type not registered in map")
    }
}

private val String.streetName: String
    get() {
        return indexOf("iela")
            .takeUnless { it == -1 }
            ?.let { substring(0, it - 1) }
            ?: ""
    }

private val Document.circlekSnapshotChunks: List<List<Element>>
    get() {
        return this
            .select("table.fuelprices tr")
            .drop(1)
            .flatMap { it.children() }
            .windowed(3, 3)
    }