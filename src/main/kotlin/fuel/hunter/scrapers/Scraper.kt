package fuel.hunter.scrapers

import fuel.hunter.Prices
import fuel.hunter.models.Station
import kotlinx.coroutines.flow.Flow
import org.jsoup.nodes.Document

interface Scraper {
    fun scrape(stations: List<Station>, document: Document): Flow<Prices>
}