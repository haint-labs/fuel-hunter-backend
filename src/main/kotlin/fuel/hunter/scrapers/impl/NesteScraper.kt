package fuel.hunter.scrapers.impl

import fuel.hunter.models.Price
import fuel.hunter.models.Station
import fuel.hunter.repo.Price2
import fuel.hunter.repo.Session
import fuel.hunter.scrapers.Scraper
import fuel.hunter.tools.toAddressRegex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document

private val fuelTypeMap = mapOf(
    "Neste Futura 95" to Price.FuelType.E95,
    "Neste Futura 98" to Price.FuelType.E98,
    "Neste Futura D" to Price.FuelType.DD,
    "Neste Pro Diesel" to Price.FuelType.DD
)

class NesteScraper : Scraper {
    override fun scrape(session: Session, stations: List<Station>, document: Document): Flow<List<Price2>> = flow {
        val prices = document
            .nesteSnapshotChunks
            .flatMap { chunk ->
                val (rawType, rawPrice, rawAddress) = chunk

                val addressParts = rawAddress
                    .split(", ")
                    .map(String::trim)

                addressParts
                    .drop(1)
                    .mapNotNull {
                        val regex = it.toAddressRegex()
                        val station = stations.find { s -> s.address.matches(regex) }
                            ?: run {
                                println("Unable to identify station - scrapper: Neste, raw address: $it")
                                return@mapNotNull null
                            }

                        Price2(
                            sessionId = session.id,
                            stationId = station.id,
                            type = fuelTypeMap[rawType].toString(),
                            price = rawPrice.toFloat()
                        )
                    }
            }

        emit(prices)
    }
}

private val Document.nesteSnapshotChunks: List<List<String>>
    get() {
        return this
            .select("tr td p")
            .eachText()
            .windowed(3, 3)
    }
