package fuel.hunter.scrapers.internal

import fuel.hunter.extensions.price
import fuel.hunter.models.Price
import fuel.hunter.repo.Repository
import fuel.hunter.tools.toAddressRegex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document

private val fuelTypeMap = mapOf(
    "miles 95" to Price.FuelType.E95,
    "milesPLUS 98" to Price.FuelType.E98,
    "miles D" to Price.FuelType.DD,
    "milesPLUS D" to Price.FuelType.DD,
    "Autogāze" to Price.FuelType.GAS
)

class CircleKScrapper(private val repo: Repository) : Scraper {
    override fun scrape(document: Document): Flow<Price> = flow {
        val stations = repo.getStations()

        val chunks = document
            .select("table tr")
            .drop(1)
            .map { it.children().map { it.text() } }

        chunks
            .map { (rawType, rawPrice, rawAddress) ->
                val regex = rawAddress.toAddressRegex()

                val station = stations
                    .find { s -> s.address.matches(regex) }

                val price = station
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
                        price = rawPrice.toFloat()
                    }

                emit(price)
            }
    }
}

