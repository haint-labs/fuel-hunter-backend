package fuel.hunter.grpc

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.FuelHunterServiceGrpcKt.FuelHunterServiceCoroutineImplBase
import fuel.hunter.dao.SnapshotDao
import fuel.hunter.extensions.priceResponse
import fuel.hunter.models.Company
import fuel.hunter.models.Price
import fuel.hunter.models.Station
import fuel.hunter.shared.Update
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.coroutine.coroutine

class FuelHunterGrpc(
    private val snapshotDao: SnapshotDao,
    private val dbClient: MongoClient
) : FuelHunterServiceCoroutineImplBase() {
    private val db by lazy {
        dbClient.coroutine.getDatabase("fuel-hunter")
    }

    override suspend fun getPrices(request: Price.Query): Price.Response {
        val snapshots = snapshotDao.getMatching(request)
        return priceResponse {
            addAllPrices(snapshots)
        }
    }

    override suspend fun getStations(request: Station.Query): Station.Response {
        val stations = db
            .getCollection<Station>("stations")
            .find()
            .toFlow()
            .toList()

        return Station.Response
            .newBuilder()
            .addAllStations(stations)
            .build()
    }

    override suspend fun updateStations(request: Station.UpdateRequest): Update.Response {
        val collection = db
            .getCollection<Station>("stations")

        val count = with(collection) {
            deleteMany()

            insertMany(request.stationsList)
                .insertedIds
                .size
        }

        return Update.Response.newBuilder()
            .setStatus(Update.Status.SUCCESS)
            .setCount(count.toLong())
            .build()
    }

    override suspend fun getCompanies(request: Company.Query): Company.Response {
        val companies = db
            .getCollection<Company>("companies")
            .find()
            .toFlow()
            .toList()

        return Company.Response
            .newBuilder()
            .addAllCompanies(companies)
            .build()
    }

    override suspend fun updateCompanies(request: Company.UpdateRequest): Update.Response {
        val collection = db
            .getCollection<Company>("companies")

        val count = with(collection) {
            deleteMany()

            insertMany(request.companiesList)
                .insertedIds
                .size
        }

        return Update.Response.newBuilder()
            .setStatus(Update.Status.SUCCESS)
            .setCount(count.toLong())
            .build()
    }
}