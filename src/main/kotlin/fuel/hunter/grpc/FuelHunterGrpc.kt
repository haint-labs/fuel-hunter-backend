package fuel.hunter.grpc

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.FuelHunterServiceGrpcKt.FuelHunterServiceCoroutineImplBase
import fuel.hunter.models.Company
import fuel.hunter.models.Price
import fuel.hunter.models.Station
import fuel.hunter.repo.Company2
import fuel.hunter.repo.Repository
import fuel.hunter.repo.Station2
import fuel.hunter.repo.toEntity
import fuel.hunter.shared.Update
import org.litote.kmongo.coroutine.coroutine

class FuelHunterGrpc(
    private val repo: Repository,
    private val dbClient: MongoClient
) : FuelHunterServiceCoroutineImplBase() {
    private val db by lazy {
        dbClient.coroutine.getDatabase("fuel-hunter")
    }

    override suspend fun getPrices(request: Price.Query): Price.Response {
        return Price.Response
            .newBuilder()
            .addAllItems(repo.getPrices(request))
            .build()
    }

    override suspend fun getStations(request: Station.Query): Station.Response {
        return Station.Response
            .newBuilder()
            .addAllStations(repo.getStations())
            .build()
    }

    override suspend fun updateStations(request: Station.UpdateRequest): Update.Response {
        val collection = db
            .getCollection<Station2>("stations")

        val count = with(collection) {
            deleteMany()

            insertMany(request.stationsList.map(Station::toEntity))
                .insertedIds
                .size
        }

        return Update.Response.newBuilder()
            .setStatus(Update.Status.SUCCESS)
            .setCount(count.toLong())
            .build()
    }

    override suspend fun getCompanies(request: Company.Query): Company.Response {
        return Company.Response
            .newBuilder()
            .addAllCompanies(repo.getCompanies())
            .build()
    }

    override suspend fun updateCompanies(request: Company.UpdateRequest): Update.Response {
        val collection = db
            .getCollection<Company2>("companies")

        val count = with(collection) {
            deleteMany()

            insertMany(request.companiesList.map(Company::toEntity))
                .insertedIds
                .size
        }

        return Update.Response.newBuilder()
            .setStatus(Update.Status.SUCCESS)
            .setCount(count.toLong())
            .build()
    }
}