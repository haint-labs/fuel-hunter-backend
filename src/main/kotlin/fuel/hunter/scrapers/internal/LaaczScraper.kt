package fuel.hunter.scrapers.internal

import fuel.hunter.Snapshot
import fuel.hunter.extensions.snapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class LaaczScraper : Scraper {
    override fun scrape(document: Document): Flow<Snapshot> = flow {
        document.laaczSnapshotChunks.forEach {
            val (addressElement, dieselElement, e98Element, gasElement, e95Element) = it

            val addressParts = (addressElement.childNode(1) as Element)
                .ownText()
                .split(", ")

            val fuelMap = mapOf(
                "Diesel" to dieselElement,
                "E95" to e95Element,
                "E98" to e98Element,
                "Gas" to gasElement
            )

            fuelMap
                .filter { (_, value) -> value.text() != "-" }
                .map { (fuelType, element) ->
                    emit(
                        snapshot {
                            provider = ""
                            name = addressElement.ownText()
                            address = addressParts[0]
                            city = addressParts[1]
                            type = fuelType
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