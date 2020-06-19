package fuel.hunter.grpc

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.Station
import fuel.hunter.StationQuery
import fuel.hunter.StationResponse
import fuel.hunter.StationServiceGrpcKt.StationServiceCoroutineImplBase
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.coroutine.coroutine

class StationGrpc(private val dbClient: MongoClient) : StationServiceCoroutineImplBase() {
    override suspend fun getStations(request: StationQuery): StationResponse {
        val stations = dbClient.coroutine
            .getDatabase("fuel-hunter")
            .getCollection<Station>("stations")
            .find()
            .toFlow()
            .toList()

        return StationResponse.newBuilder()
            .addAllStations(stations)
            .build()
    }
}