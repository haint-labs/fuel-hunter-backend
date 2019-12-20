//package fuel.hunter.scrapers
//
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.*
//import java.util.logging.Logger
//
//@ExperimentalCoroutinesApi
//class ScraperService(
//    private val scrapers: List<Pair<String, Scraper>>,
//    private val documentProvider: DocumentProvider
//) {
//    private val scope = CoroutineScope(Dispatchers.IO)
//
//    private val logger = Logger.getLogger("Scrapper")
//
//    fun run() {
//        logger.info("Starting scrapper")
//
//        val snapshots = scrapers
//            .map { (url, scraper) -> scraper.scrape(url, documentProvider) }
//            .merge()
//
//        snapshots
//            .onStart { logger.info("Scraping...") }
//            .onEach { logger.info(it.toString()) }
//            .onCompletion { logger.info("Done") }
//            .launchIn(scope)
//    }
//}
//
