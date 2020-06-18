package fuel.hunter

import fuel.hunter.dao.InMemorySnapshotDao
import fuel.hunter.grpc.SnapshotGrpc
import fuel.hunter.rest.Company
import fuel.hunter.rest.Station
import fuel.hunter.rest.launchRestService
import fuel.hunter.scrapers.internal.CircleKScrapper
import fuel.hunter.scrapers.internal.LaaczScraper
import fuel.hunter.scrapers.internal.NesteScraper
import fuel.hunter.service.launchScrappers
import fuel.hunter.service.launchStorage
import io.grpc.ServerBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.litote.kmongo.reactivestreams.KMongo

@ExperimentalCoroutinesApi
fun main(args: Array<String>) {
    val config = getConfiguration(args.firstOrNull())

    val documentProvider = config.provider
    val scrapers = mapOf(
        "https://www.neste.lv/lv/content/degvielas-cenas" to NesteScraper(),
        "https://www.circlek.lv/lv_LV/pg1334072578525/private/Degviela/Cenas.html" to CircleKScrapper(),
        "https://laacz.lv/f/misc/gas-prices.php" to LaaczScraper()
    )

    val dbClient = KMongo.createClient(config.database)

    val memory = mutableListOf<Snapshot>()
    val snapshots = Channel<Snapshot>(100)

    GlobalScope.launch {
        launchScrappers(documentProvider, config.dataFeedRefreshInterval, scrapers, snapshots)
        launchStorage(snapshots, memory)

        launchRestService(dbClient)
    }

    val snapshotDao = InMemorySnapshotDao(memory)
    val grpcService = SnapshotGrpc(snapshotDao)

    println("[SERVER] Starting with configuration - ${config}...")
    ServerBuilder.forPort(config.port)
        .addService(grpcService)
        .build()
        .start()
        .awaitTermination()
}
