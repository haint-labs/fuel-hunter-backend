package fuel.hunter.repo

import com.mongodb.client.model.Accumulators.addToSet
import com.mongodb.client.model.Accumulators.push
import com.mongodb.client.model.Aggregates.*
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.Filters.and
import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.models.Company
import fuel.hunter.models.Price
import fuel.hunter.models.Station
import fuel.hunter.tools.bson
import fuel.hunter.tools.geoNear
import fuel.hunter.tools.round
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import org.bson.*
import org.bson.conversions.Bson

interface Repository {
    suspend fun saveSession(session: Session)

    suspend fun getStations(): List<Station>

    suspend fun getCompanies(): List<Company>

    suspend fun getPrices(query: Price.Query): List<Price.Response.Item>
}

class MongoRepository(
    private val dbClient: MongoClient
) : Repository {
    private val db by lazy {
        dbClient.getDatabase("fuel-hunter")
    }

    override suspend fun saveSession(session: Session) {
        db.getCollection("sessions", Session::class.java)
            .insertOne(session)
            .awaitFirst()
    }

    override suspend fun getStations(): List<Station> {
        return db.getCollection("stations", Station2::class.java)
            .find()
            .asFlow()
            .toList()
            .map(Station2::fromEntity)
    }

    override suspend fun getCompanies(): List<Company> {
        return db.getCollection("companies", Company2::class.java)
            .find()
            .asFlow()
            .toList()
            .map(Company2::fromEntity)
    }

    override suspend fun getPrices(query: Price.Query): List<Price.Response.Item> {
        val lastSession = db.getCollection("sessions", Session::class.java)
            .find()
            .sort(bson("timestamp" to -1))
            .limit(1)
            .awaitFirst()

        val pipe = mutableListOf<Bson>()

        // grab by location
        if (query.hasLocation()) {
            pipe += geoNear(
                near = listOf(
                    query.location.latitude,
                    query.location.longitude
                ),
                maxDistance = query.distance.takeIf { it != 0f } ?: 2000f,
                key = "location"
            )
        }

        // filter stations
        mutableListOf<Bson>()
            .apply {
                if (query.cityCount > 0) {
                    this += `in`("city", query.cityList)
                }

                if (query.stationIdCount > 0) {
                    this += `in`("_id", query.stationIdList)
                }
            }
            .takeIf { it.isNotEmpty() }
            ?.let { pipe += match(and(it)) }

        // join with prices
        pipe += lookup(
            "prices",
            listOf(match(bson("sessionId" to lastSession.id))),
            "prices"
        )

        // flat map
        pipe += unwind("\$prices")

        // filter scrapping session
        pipe += match(
            bson("prices.sessionId" to lastSession.id)
        )

        // filter fuel types
        if (query.typeCount > 0) {
            pipe += match(`in`("prices.type", query.typeList))
        }

        // group by type, price and company
        pipe += group(
            bson(
                "type" to "\$prices.type",
                "price" to round("\$prices.price", 3),
                "company" to "\$company"
            ),
            addToSet("stations", "\$_id")
        )

        // sort by price
        pipe += sort(
            bson("_id.price" to 1)
        )

        // group type
        pipe += group(
            bson("type" to "\$_id.type"),
            push(
                "prices",
                bson(
                    "price" to "\$_id.price",
                    "company" to "\$_id.company",
                    "stations" to "\$stations",
                )
            )
        )

        // reshape
        pipe += project(
            bson(
                "_id" to false,
                "type" to "\$_id.type",
                "prices" to true,
            )
        )

        return dbClient
            .getDatabase("fuel-hunter")
            .getCollection("stations")
            .aggregate(pipe)
            .asFlow()
            .map { item ->
                val fuelType = Price.FuelType.valueOf(item.getString("type"))
                val prices = item.getList("prices", Document::class.java)
                    .map {
                        Price.Response.CompanyPriceGrouped.newBuilder()
                            .setCompany(it.getString("company"))
                            .setPrice(it.getDouble("price").toFloat())
                            .addAllStations(it.getList("stations", String::class.java))
                            .build()
                    }

                Price.Response.Item.newBuilder()
                    .setType(fuelType)
                    .addAllPrices(prices)
                    .build()
            }
            .toList()
    }
}
