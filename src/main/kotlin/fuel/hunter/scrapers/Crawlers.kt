package fuel.hunter.scrapers

import org.jsoup.nodes.Document

interface DocumentProvider {
    fun getDocument(uri: String): Document
}

