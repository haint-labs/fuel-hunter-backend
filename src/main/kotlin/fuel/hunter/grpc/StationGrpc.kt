package fuel.hunter.grpc

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.Station
import fuel.hunter.StationQuery
import fuel.hunter.StationResponse
import fuel.hunter.StationServiceGrpcKt.StationServiceCoroutineImplBase
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.coroutine.coroutine

class StationGrpc(private val dbClient: MongoClient) : StationServiceCoroutineImplBase() {
    override suspend fun getStations(request: StationQuery): StationResponse {
        val stations = dbClient.coroutine
            .getDatabase("fuel-hunter")
            .getCollection<fuel.hunter.rest.Station>("stations")
            .find()
            .toFlow()
            .map {
                // TODO: how to avoid duplicates
                Station.newBuilder()
                    .setId(it.id.toDouble())
                    .setCompany(it.company)
                    .setLatitude(it.latitude)
                    .setLongitude(it.longitude)
                    .setAddress(it.address)
                    .setCity(it.city)
                    .setName(it.name)
                    .build()
            }
            .toList()

        return StationResponse.newBuilder()
            .addAllStations(stations)
            .build()
    }
}