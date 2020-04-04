package fuel.hunter.scrapers.internal

import fuel.hunter.scrapers.DocumentProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class OnlineDocumentProvider : DocumentProvider {
    override fun getDocument(uri: String): Document = Jsoup.connect(uri).get()
}

class OfflineDocumentProvider : DocumentProvider {
    override fun getDocument(uri: String): Document {
        val resourceName = when {
            uri.contains("neste") -> "snapshots/offline/neste.htm"
            uri.contains("circlek") -> "snapshots/offline/circlek.htm"
            else -> throw IllegalArgumentException("Unknown page provided")
        }

        val url = requireNotNull(javaClass.classLoader.getResource(resourceName))
        val contents = url.readText()

        return Jsoup.parse(contents)
    }
}