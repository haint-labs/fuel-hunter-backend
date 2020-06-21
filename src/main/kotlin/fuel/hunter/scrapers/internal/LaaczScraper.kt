package fuel.hunter.scrapers.internal

import fuel.hunter.models.Snapshot
import fuel.hunter.extensions.snapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class LaaczScraper : Scraper {
    override fun scrape(document: Document): Flow<Snapshot> = flow {
        val fuelTypeMap = document
            .select("table.sortable thead th")
            .drop(1)
            .withIndex()
            .associateBy(
                keySelector = { it.index },
                valueTransform = { it.value.ownText() }
            )

        document.laaczSnapshotChunks.forEach { chunk ->
            val addressElement = chunk.first()
            val addressParts = (addressElement.childNode(1) as Element)
                .ownText()
                .split(", ")
                .takeIf { it.size == 2 }
                ?: return@forEach

            chunk
                .drop(1)
                .forEachIndexed { index, element ->
                    element.takeIf { it.text() != "-" }
                        ?: return@forEachIndexed

                    emit(
                        snapshot {
                            provider = ""
                            name = addressElement.ownText()
                            address = addressParts[0]
                            city = addressParts[1]
                            type = fuelTypeMap[index]
                            price = element.text().toFloat()
                        }
                    )
                }
        }
    }
}

private val Document.laaczSnapshotChunks: List<List<Element>>
    get() {
        return select("table.sortable > tbody tr")
            .flatMap { it.children() }
            .windowed(5, 5)
    }