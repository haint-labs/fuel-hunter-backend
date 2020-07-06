package fuel.hunter.scrapers.internal

import fuel.hunter.models.Price
import fuel.hunter.extensions.price
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

class NesteScraper : Scraper {
    override fun scrape(document: Document): Flow<Price> = flow {
        document.nesteSnapshotChunks.flatMap { chunk ->
            val (rawType, rawPrice, rawAddress) = chunk

            val addressParts = rawAddress
                .split(", ")
                .map(String::trim)

            addressParts.drop(1).map {
                val item = price {
                    name = "Neste, $it"
                    address = it
                    city = addressParts[0]
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
