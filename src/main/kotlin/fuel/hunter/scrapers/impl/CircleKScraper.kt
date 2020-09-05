package fuel.hunter.scrapers.impl

import fuel.hunter.Prices
import fuel.hunter.extensions.price
import fuel.hunter.models.Price.FuelType.*
import fuel.hunter.models.Station
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
    override fun scrape(stations: List<Station>, document: Document): Flow<Prices> = flow {
        val chunks = document
            .select("table tr")
            .drop(1)
            .map { it.children().map { it.text() } }

        val prices = chunks
            .map { (rawType, rawPrice, rawAddress) ->
                val regex = rawAddress.toAddressRegex()

                val station = stations
                    .find { s -> s.address.matches(regex) }

                station
                    ?.let {
                        price {
                            name = station.name
                            address = station.address
                            city = station.city
                            stationId = station.id
                            type = fuelTypeMap[rawType]
                            price = rawPrice.replace(" EUR", "").toFloat()
                        }
                    }
                    ?: price {
                        name = "Circle K, $rawAddress"
                        address = rawAddress
                        city = "Rīga"
                        stationId = ""
                        type = fuelTypeMap[rawType]
                        price = rawPrice.replace(" EUR", "").toFloat()
                    }
            }

        emit(prices)
    }
}
