package fuel.hunter.repo

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.models.Station
import org.litote.kmongo.coroutine.coroutine

interface Repository {
    suspend fun getStations(): List<Station>
}

data class Point(
    val coordinates: List<Float>,
    val type: String = "Point"
)

data class Station2(
    val id: String,
    val company: String,
    val location: Point,
    val address: String,
    val city: String,
    val name: String
)

fun Station.toEntity() = Station2(id, company, Point(listOf(longitude, latitude)), address, city, name)

fun Station2.fromEntity(): Station =
    Station.newBuilder()
        .setId(id)
        .setCompany(company)
        .setLongitude(location.coordinates[0])
        .setLatitude(location.coordinates[1])
        .setAddress(address)
        .setCity(city)
        .setName(name)
        .build()

class MongoRepository(
    private val dbClient: MongoClient
) : Repository {
    private val db by lazy {
        dbClient.coroutine.getDatabase("fuel-hunter")
    }

    override suspend fun getStations(): List<Station> {
        return db.getCollection<Station2>("stations")
            .find()
            .toList()
            .map(Station2::fromEntity)
    }
}