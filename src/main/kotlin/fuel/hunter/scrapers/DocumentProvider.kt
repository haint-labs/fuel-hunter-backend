package fuel.hunter.scrapers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

interface DocumentProvider {
    fun getDocument(uri: String): Document
}

class OnlineDocumentProvider : DocumentProvider {
    override fun getDocument(uri: String): Document = Jsoup.connect(uri).get()
}

class OfflineDocumentProvider : DocumentProvider {
    override fun getDocument(uri: String): Document {
        val resourceName = when {
            uri.contains("neste") -> "snapshots/offline/neste.htm"
            uri.contains("circlek") -> "snapshots/offline/circlek.htm"
            uri.contains("laacz") -> "snapshots/offline/laacz.html"
            else -> throw IllegalArgumentException("Unknown page provided")
        }

        val url = getResource(resourceName)
        val contents = url.readText()

        return Jsoup.parse(contents)
    }
}