package fuel.hunter

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClients
import fuel.hunter.grpc.FuelHunterGrpc
import fuel.hunter.models.Price
import fuel.hunter.repo.MongoRepository
import fuel.hunter.repo.Repository
import fuel.hunter.scrapers.internal.CircleKScrapper
import fuel.hunter.scrapers.internal.LaaczScraper
import fuel.hunter.scrapers.internal.NesteScraper
import fuel.hunter.service.launchScrappers
import fuel.hunter.service.launchStorage
import io.github.gaplotech.PBCodecProvider
import io.grpc.ServerBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.bson.codecs.configuration.CodecRegistries
import org.litote.kmongo.reactivestreams.KMongo

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

    val snapshots = Channel<Price>(100)

    val repo: Repository = MongoRepository(dbClient)

    val documentProvider = config.provider
    val scrapers = mapOf(
        "https://www.neste.lv/lv/content/degvielas-cenas" to NesteScraper(),
        "https://www.circlek.lv/lv_LV/pg1334072578525/private/Degviela/Cenas.html" to CircleKScrapper(),
        "https://laacz.lv/f/misc/gas-prices.php" to LaaczScraper(repo)
    )

    GlobalScope.launch {
        launchScrappers(documentProvider, config.dataFeedRefreshInterval, scrapers, snapshots)
        launchStorage(snapshots, dbClient)
    }

    val fuelHunter = FuelHunterGrpc(dbClient)

    ServerBuilder.forPort(config.port)
        .addService(fuelHunter)
        .build()
        .start()
        .awaitTermination()
}
