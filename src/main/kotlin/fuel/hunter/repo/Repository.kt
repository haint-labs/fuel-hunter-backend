package fuel.hunter.repo

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.models.Station
import org.litote.kmongo.coroutine.coroutine

interface Repository {
    suspend fun getStations(): List<Station>
}

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