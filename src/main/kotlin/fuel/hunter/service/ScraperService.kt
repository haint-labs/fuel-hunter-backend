package fuel.hunter.service

import fuel.hunter.repo.Price2
import fuel.hunter.repo.Repository
import fuel.hunter.repo.Session
import fuel.hunter.scrapers.DocumentProvider
import fuel.hunter.scrapers.Scraper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.util.*

@ExperimentalCoroutinesApi
fun CoroutineScope.launchScrappers(
    documentProvider: DocumentProvider,
    repo: Repository,
    dataFeedRefreshInterval: Int,
    scrapers: Map<String, Scraper>,
    snapshots: SendChannel<List<Price2>>
) = launch {
    println("[SCRAPPERS] Started...")

    val random = Random()

    while (!snapshots.isClosedForSend) {
        val session = Session(
            timestamp = System.currentTimeMillis()
        )

        repo.saveSession(session)

        scrapers
            .entries
            .mapNotNull { (url, scrapper) ->
                val scrapperName = scrapper.javaClass.simpleName
                println("[SCRAPPERS] Hunting data - $scrapperName")

                try {
                    val stations = repo.getStations()
                    val document = documentProvider.getDocument(url)

                    scrapper.scrape(session, stations, document)
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