package fuel.hunter

import fuel.hunter.dao.InMemorySnapshotDao
import fuel.hunter.scrapers.internal.NesteScraper
import fuel.hunter.scrapers.internal.OfflineDocumentProvider
import fuel.hunter.grpc.SnapshotGrpc
import fuel.hunter.service.launchScrappers
import fuel.hunter.service.launchStorage
import io.grpc.ServerBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
fun main() {
    val documentProvider = OfflineDocumentProvider()
    val scrapers = mapOf(
        "https://www.neste.lv/lv/content/degvielas-cenas" to NesteScraper(),
        "https://www.neste.lv/lv/content/degvielas-cenas1" to NesteScraper(),
        "https://www.neste.lv/lv/content/degvielas-cenas2" to NesteScraper()
    )

    val memory = mutableListOf<Snapshot>()
    val snapshots = Channel<Snapshot>(50)

    GlobalScope.launch {
        launchScrappers(documentProvider, scrapers, snapshots)
        launchStorage(snapshots, memory)
    }

    val snapshotDao = InMemorySnapshotDao(memory)
    val grpcService = SnapshotGrpc(snapshotDao)

    ServerBuilder.forPort(50051)
        .addService(grpcService)
        .build()
        .start()
        .awaitTermination()
}
