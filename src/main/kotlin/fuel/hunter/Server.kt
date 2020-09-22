package fuel.hunter

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClients
import fuel.hunter.grpc.FuelHunterGrpc
import fuel.hunter.models.Price
import fuel.hunter.models.Station
import fuel.hunter.repo.MongoRepository
import fuel.hunter.repo.Price2
import fuel.hunter.repo.Repository
import fuel.hunter.scrapers.impl.CircleKScraper
import fuel.hunter.scrapers.impl.LaaczScraper
import fuel.hunter.scrapers.impl.NesteScraper
import fuel.hunter.service.launchScrappers
import fuel.hunter.service.launchStorage
import io.github.gaplotech.PBCodecProvider
import io.grpc.ServerBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.bson.codecs.configuration.CodecRegistries
import org.litote.kmongo.reactivestreams.KMongo

typealias Prices = List<Price>
typealias Stations = List<Station>

@ExperimentalCoroutinesApi
fun main(args: Array<String>) {
    val config = getConfiguration(args.firstOrNull())
    println("[SERVER] Starting with configuration - ${config}...")

    val dbSettings = MongoClientSettings
        .builder()
        .applyConnectionString(ConnectionString(config.database))
        .codecRegistry(
            CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(
                    PBCodecProvider(preservingProtoFieldNames = false)
                ),
                MongoClients.getDefaultCodecRegistry()
            )
        )
        .build()
    val dbClient = KMongo.createClient(dbSettings)

    val prices = Channel<List<Price2>>(100)

    val repo: Repository = MongoRepository(dbClient)

    val documentProvider = config.provider
    val scrapers = mapOf(
        "https://www.neste.lv/lv/content/degvielas-cenas" to NesteScraper(),
        "https://www.circlek.lv/degvielas-cenas" to CircleKScraper(),
        "https://laacz.lv/f/misc/gas-prices.php" to LaaczScraper()
    )

    GlobalScope.launch {
        launchScrappers(documentProvider, repo, config.dataFeedRefreshInterval, scrapers, prices)
        launchStorage(prices.receiveAsFlow(), dbClient)
    }

    val fuelHunter = FuelHunterGrpc(repo, dbClient)

    ServerBuilder.forPort(config.port)
        .addService(fuelHunter)
        .build()
        .start()
        .awaitTermination()
}
