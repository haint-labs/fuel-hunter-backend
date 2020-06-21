package fuel.hunter.grpc

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.FuelHunterServiceGrpcKt.FuelHunterServiceCoroutineImplBase
import fuel.hunter.dao.SnapshotDao
import fuel.hunter.extensions.snapshotResponse
import fuel.hunter.models.Company
import fuel.hunter.models.Snapshot
import fuel.hunter.models.Station
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.coroutine.coroutine

class FuelHunterGrpc(
    private val snapshotDao: SnapshotDao,
    private val dbClient: MongoClient
) : FuelHunterServiceCoroutineImplBase() {

    override suspend fun getSnapshots(request: Snapshot.Query): Snapshot.Response {
        val snapshots = snapshotDao.getMatching(request)
        return snapshotResponse {
            addAllSnapshots(snapshots)
        }
    }

    override suspend fun getStations(request: Station.Query): Station.Response {
        val stations = dbClient.coroutine
            .getDatabase("fuel-hunter")
            .getCollection<Station>("stations")
            .find()
            .toFlow()
            .toList()

        return Station.Response
            .newBuilder()
            .addAllStations(stations)
            .build()
    }

    override suspend fun getCompanies(request: Company.Query): Company.Response {
        val companies = dbClient.coroutine
            .getDatabase("fuel-hunter")
            .getCollection<Company>("companies")
            .find()
            .toFlow()
            .toList()

        return Company.Response
            .newBuilder()
            .addAllCompanies(companies)
            .build()
    }
}