package fuel.hunter.scrapers.internal

import fuel.hunter.extensions.price
import fuel.hunter.models.Price
import fuel.hunter.repo.Repository
import fuel.hunter.tools.toAddressRegex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document

interface Scraper {
    fun scrape(document: Document): Flow<Price>
}

private val fuelTypeMap = mapOf(
    "Neste Futura 95" to Price.FuelType.E95,
    "Neste Futura 98" to Price.FuelType.E98,
    "Neste Futura D" to Price.FuelType.DD,
    "Neste Pro Diesel" to Price.FuelType.DD
)

class NesteScraper(private val repo: Repository) : Scraper {
    override fun scrape(document: Document): Flow<Price> = flow {
        val stations = repo.getStations()

        document.nesteSnapshotChunks.flatMap { chunk ->
            val (rawType, rawPrice, rawAddress) = chunk

            val addressParts = rawAddress
                .split(", ")
                .map(String::trim)

            addressParts.drop(1).map {

                val regex = it.toAddressRegex()

                val station = stations.find { s -> s.address.matches(regex) }

                val item = station
                    ?.let {
                        price {
                            name = station.name
                            address = station.address
                            city = station.city
                            stationId = station.id
                            type = fuelTypeMap[rawType]
                            price = rawPrice.toFloat()
                        }
                    }
                    ?: price {
                        name = "Neste, $it"
                        address = it
                        city = addressParts[0]
                        stationId = ""
                        type = fuelTypeMap[rawType]
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
