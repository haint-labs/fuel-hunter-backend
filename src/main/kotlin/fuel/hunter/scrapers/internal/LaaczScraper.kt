package fuel.hunter.scrapers.internal

import fuel.hunter.extensions.price
import fuel.hunter.models.Price
import fuel.hunter.repo.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

private val fuelTypeMap = mapOf(
    "95" to Price.FuelType.E95,
    "98" to Price.FuelType.E98,
    "diesel" to Price.FuelType.DD,
    "lpg" to Price.FuelType.GAS
)

class LaaczScraper(private val repo: Repository) : Scraper {
    override fun scrape(document: Document): Flow<Price> = flow {
        val fuelTypeMap = document
            .select("table.sortable thead th")
            .drop(1)
            .withIndex()
            .associateBy(
                keySelector = { it.index },
                valueTransform = { fuelTypeMap[it.value.ownText()] }
            )

        val stations = repo.getStations()

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

                    val station = stations.find { s -> s.address == addressParts[0] }
                            ?: stations.find { s -> s.name == addressElement.ownText() }

                    val price = station
                        ?.let {
                            price {
                                name = it.name
                                address = it.address
                                city = it.city
                                stationId = it.id
                                type = fuelTypeMap[index]
                                price = element.text().toFloat()
                            }
                        }
                        ?: price {
                            name = addressElement.ownText()
                            address = addressParts[0]
                            city = addressParts[1]
                            stationId = ""
                            type = fuelTypeMap[index]
                            price = element.text().toFloat()
                        }

                    emit(price)
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