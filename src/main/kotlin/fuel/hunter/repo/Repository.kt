package fuel.hunter.repo

import com.mongodb.client.model.Accumulators.addToSet
import com.mongodb.client.model.Aggregates.*
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.Filters.and
import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.models.Price
import fuel.hunter.models.Station
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import org.bson.*
import org.bson.conversions.Bson

interface Repository {
    suspend fun saveSession(session: Session)

    suspend fun getStations(): List<Station>

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

    override suspend fun getPrices(query: Price.Query): List<Price.Response.Item> {
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
        pipe += lookup("prices", "_id", "stationId", "prices")

        // flat map
        pipe += unwind("\$prices")

        // filter fuel types
        if (query.typeCount > 0) {
            pipe += match(`in`("prices.type", query.typeList))
        }

        // group by stations
        pipe += group(
            "\$_id",
            addToSet(
                "prices",
                BsonDocument()
                    .append("price", round("\$prices.price", 3))
                    .append("type", BsonString("\$prices.type"))
            )
        )

        // reshape
        pipe += project(
            BsonDocument()
                .append("_id", BsonBoolean(false))
                .append("stationId", BsonString("\$_id"))
                .append("prices", BsonBoolean(true))
        )

        println("request - $pipe")

        return dbClient
            .getDatabase("fuel-hunter")
            .getCollection("stations")
            .aggregate(pipe, Price.Response.Item::class.java)
            .asFlow()
            .toList()
    }
}

fun geoNear(
    near: List<Float>,
    maxDistance: Float,
    key: String,
    distanceField: String = "distance",
    spherical: Boolean = true
): Bson {
    val coordinates = near
        .map(Float::toDouble)
        .map(::BsonDouble)

    val point = BsonDocument()
        .append("type", BsonString("Point"))
        .append("coordinates", BsonArray(coordinates))

    val params = BsonDocument()
        .append("near", point)
        .append("key", BsonString(key))
        .append("maxDistance", BsonDouble(maxDistance.toDouble()))
        .append("distanceField", BsonString(distanceField))
        .append("spherical", BsonBoolean(spherical))

    return BsonDocument("\$geoNear", params)
}

fun round(field: String, place: Int): BsonDocument {
    val params = BsonArray().apply {
        add(BsonString(field))
        add(BsonInt32(place))
    }

    return BsonDocument("\$round", params)
}