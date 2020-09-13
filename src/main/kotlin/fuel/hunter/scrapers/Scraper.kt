package fuel.hunter.scrapers

import fuel.hunter.models.Station
import fuel.hunter.repo.Price2
import fuel.hunter.repo.Session
import kotlinx.coroutines.flow.Flow
import org.jsoup.nodes.Document

interface Scraper {
    fun scrape(session: Session, stations: List<Station>, document: Document): Flow<List<Price2>>
}