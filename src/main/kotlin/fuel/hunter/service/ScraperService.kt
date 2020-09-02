package fuel.hunter.service

import fuel.hunter.Prices
import fuel.hunter.repo.Repository
import fuel.hunter.scrapers.DocumentProvider
import fuel.hunter.scrapers.internal.Scraper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

@ExperimentalCoroutinesApi
fun CoroutineScope.launchScrappers(
    documentProvider: DocumentProvider,
    repo: Repository,
    dataFeedRefreshInterval: Int,
    scrapers: Map<String, Scraper>,
    snapshots: SendChannel<Prices>
) = launch {
    println("[SCRAPPERS] Started...")

    val random = Random()

    while (!snapshots.isClosedForSend) {
        scrapers
            .entries
            .mapNotNull { (url, scrapper) ->
                val scrapperName = scrapper.javaClass.simpleName
                println("[SCRAPPERS] Hunting data - $scrapperName")

                try {
                    val stations = repo.getStations()
                    val document = documentProvider.getDocument(url)

                    scrapper.scrape(stations, document)
                } catch (e: Exception) {
                    println("[SCRAPPERS] Failed to get document - scraper: $scrapperName, url: $url")
                    e.printStackTrace()
                    null
                }
            }
            .merge()
            .collect { snapshots.send(it) }

        val randomizedDelay = dataFeedRefreshInterval + random.nextInt(100) * 100L
        println("[SCRAPPERS] Suspending - wait time: $randomizedDelay")
        delay(randomizedDelay)
    }

    println("[SCRAPPERS] Closed")
}