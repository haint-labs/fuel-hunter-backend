package fuel.hunter.service

import fuel.hunter.Snapshot
import fuel.hunter.scrapers.DocumentProvider
import fuel.hunter.scrapers.internal.Scraper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalCoroutinesApi
fun CoroutineScope.launchScrappers(
    documentProvider: DocumentProvider,
    scrapers: Map<String, Scraper>,
    snapshots: SendChannel<Snapshot>
) = launch {
    println("[SCRAPPERS] Started...")

    val random = Random()

    while (!snapshots.isClosedForSend) {
        scrapers.entries.forEachIndexed { index, (url, scrapper) ->
            launch {
                println("[SCRAPPERS] Hunting data ($index) - ${scrapper.javaClass.simpleName}")

                val document = documentProvider.getDocument(url)
                scrapper.scrape(document).collect {
                    snapshots.send(it)
                    println("[SCRAPPERS] Pushing new snapshot - ${it.name}, ${it.type}: ${it.price}")
                }
            }
        }

        val randomizedDelay = 5 * 10L + random.nextInt(100) * 100L
        delay(randomizedDelay)
    }

    println("[SCRAPPERS] Closed")
}