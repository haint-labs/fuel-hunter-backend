package fuel.hunter.scrapers.impl

import fuel.hunter.models.Price.FuelType.*
import fuel.hunter.models.Station
import fuel.hunter.repo.Price2
import fuel.hunter.repo.Session
import fuel.hunter.scrapers.Scraper
import fuel.hunter.tools.toAddressRegex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document

private val fuelTypeMap = mapOf(
    "miles 95" to E95,
    "milesPLUS 98" to E98,
    "miles D" to DD,
    "milesPLUS D" to DD,
    "Autogāze" to GAS
)

class CircleKScraper : Scraper {
    override fun scrape(session: Session, stations: List<Station>, document: Document): Flow<List<Price2>> = flow {
        val chunks = document
            .select("table tr")
            .drop(1)
            .map { it.children().map { it.text() } }

        val prices = chunks
            .flatMap { (rawType, rawPrice, rawAddress) ->
                rawAddress
                    .split(",")
                    .mapNotNull {
                        val regex = it.trim().toAddressRegex()
                        val station = stations.find { s -> s.address.matches(regex) }
                            ?: run {
                                println("Unable to identify station - scrapper: Circle K, raw address: $it, regex: $regex")
                                return@mapNotNull null
                            }

                        Price2(
                            sessionId = session.id,
                            stationId = station.id,
                            type = fuelTypeMap[rawType].toString(),
                            price = rawPrice.replace(" EUR", "").toFloat()
                        )
                    }
            }

        emit(prices)
    }
}

