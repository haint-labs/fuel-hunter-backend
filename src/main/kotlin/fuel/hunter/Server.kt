package fuel.hunter

import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClients
import fuel.hunter.dao.InMemorySnapshotDao
import fuel.hunter.grpc.SnapshotGrpc
import fuel.hunter.grpc.StationGrpc
import fuel.hunter.rest.launchRestService
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

    val documentProvider = config.provider
    val scrapers = mapOf(
        "https://www.neste.lv/lv/content/degvielas-cenas" to NesteScraper(),
        "https://www.circlek.lv/lv_LV/pg1334072578525/private/Degviela/Cenas.html" to CircleKScrapper(),
        "https://laacz.lv/f/misc/gas-prices.php" to LaaczScraper()
    )

    val dbSettings = MongoClientSettings
        .builder()
        .codecRegistry(
            CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(PBCodecProvider()),
                MongoClients.getDefaultCodecRegistry()
            )
        )
        .build()
    val dbClient = KMongo.createClient(dbSettings)

    val memory = mutableListOf<Snapshot>()
    val snapshots = Channel<Snapshot>(100)

    GlobalScope.launch {
        launchScrappers(documentProvider, config.dataFeedRefreshInterval, scrapers, snapshots)
        launchStorage(snapshots, memory)

        launchRestService(dbClient)
    }

    val snapshotDao = InMemorySnapshotDao(memory)
    val snapshotGrpcService = SnapshotGrpc(snapshotDao)
    val stationsGrpcService = StationGrpc(dbClient)

    ServerBuilder.forPort(config.port)
        .addService(snapshotGrpcService)
        .addService(stationsGrpcService)
        .build()
        .start()
        .awaitTermination()
}
