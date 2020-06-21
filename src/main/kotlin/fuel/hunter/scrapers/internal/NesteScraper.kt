package fuel.hunter.scrapers.internal

import fuel.hunter.models.Snapshot
import fuel.hunter.extensions.snapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document

interface Scraper {
    fun scrape(document: Document): Flow<Snapshot>
}

class NesteScraper : Scraper {
    override fun scrape(document: Document): Flow<Snapshot> = flow {
        document.nesteSnapshotChunks.flatMap { chunk ->
            val (rawType, rawPrice, rawAddress) = chunk

            val addressParts = rawAddress
                .split(", ")
                .map(String::trim)

            addressParts.drop(1).map {
                val item = snapshot {
                    provider = "Neste"
                    name = "Neste, $it"
                    address = it
                    city = addressParts[0]
                    type = rawType
                    price = rawPrice.toFloat()
                }

                emit(item)
            }
        }
    }
}

private val Document.nesteSnapshotChunks: List<List<String>>
    get() {
        return this
            .select("tr td p")
            .eachText()
            .windowed(3, 3)
    }
