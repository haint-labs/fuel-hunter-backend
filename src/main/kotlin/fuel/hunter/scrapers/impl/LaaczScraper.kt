package fuel.hunter.scrapers.impl

import fuel.hunter.models.Price
import fuel.hunter.models.Station
import fuel.hunter.repo.Price2
import fuel.hunter.repo.Session
import fuel.hunter.scrapers.Scraper
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

class LaaczScraper : Scraper {
    override fun scrape(session: Session, stations: List<Station>, document: Document): Flow<List<Price2>> = flow {
        val fuelTypeMap = document
            .select("table.sortable thead th")
            .drop(1)
            .withIndex()
            .associateBy(
                keySelector = { it.index },
                valueTransform = { fuelTypeMap[it.value.ownText()] }
            )

        val prices = document
            .laaczSnapshotChunks
            .flatMap { chunk ->
                val addressElement = chunk.first()
                val addressParts = (addressElement.childNode(1) as Element)
                    .ownText()
                    .split(", ")
                    .takeIf { it.size == 2 }
                    ?: return@flatMap emptyList<Price2>()

                chunk
                    .drop(1)
                    .mapIndexedNotNull { index, element ->
                        element.takeIf { it.text() != "-" }
                            ?: return@mapIndexedNotNull null

                        val station = stations.find { s -> s.address == addressParts[0] }
                            ?: stations.find { s -> s.name == addressElement.ownText() }
                            ?: run {
                                println("Unable to identify station - scrapper: Laacz, raw address: ${addressParts[0]}")
                                return@mapIndexedNotNull null
                            }

                        Price2(
                            sessionId = session.id,
                            stationId = station.id,
                            type = fuelTypeMap[index].toString(),
                            price = element.text().toFloat()
                        )
                    }
            }

        emit(prices)
    }
}

private val Document.laaczSnapshotChunks: List<List<Element>>
    get() {
        return select("table.sortable > tbody tr")
            .flatMap { it.children() }
            .windowed(5, 5)
    }