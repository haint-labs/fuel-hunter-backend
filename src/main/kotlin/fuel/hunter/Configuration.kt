package fuel.hunter

import fuel.hunter.scrapers.DocumentProvider
import fuel.hunter.scrapers.internal.OfflineDocumentProvider
import java.util.*

private const val PROP_PORT = "fuel.hunter.port"
private const val PROP_DATA_FEED_REFRESH_INTERVAL = "fuel.hunter.dataFeedRefreshInterval"
private const val PROP_PROVIDER = "fuel.hunter.provider"

data class Configuration(
    val port: Int,
    val dataFeedRefreshInterval: Int,
    val provider: DocumentProvider
) {
    override fun toString() =
        "port: $port, refresh interval: $dataFeedRefreshInterval, provider: ${provider.javaClass.simpleName}"
}

private val defaultConfiguration = Configuration(
    port = 50051,
    dataFeedRefreshInterval = 5 * 60 * 60 * 1000,
    provider = OfflineDocumentProvider()
)

fun getConfiguration(path: String? = null): Configuration {
    path ?: return defaultConfiguration

    val classLoader = object {}.javaClass.classLoader

    val stream = classLoader.getResourceAsStream(path)
        ?: return defaultConfiguration

    return stream.use {
        val properties = Properties()
        properties.load(it)

        val port = properties
            .getProperty(PROP_PORT, "${defaultConfiguration.port}")
            .toInt()

        val dataFeedRefreshInterval = properties
            .getProperty(PROP_DATA_FEED_REFRESH_INTERVAL)
            ?.toIntOrNull()
            ?: defaultConfiguration.dataFeedRefreshInterval

        val provider = properties
            .getProperty(PROP_PROVIDER)
            ?.let { providerClass ->
                classLoader.loadClass(providerClass)
                    .getDeclaredConstructor()
                    .newInstance() as DocumentProvider
            }
            ?: defaultConfiguration.provider

        Configuration(port, dataFeedRefreshInterval, provider)
    }
}