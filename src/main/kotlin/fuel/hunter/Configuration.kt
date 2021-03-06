package fuel.hunter

import fuel.hunter.scrapers.DocumentProvider
import fuel.hunter.scrapers.OfflineDocumentProvider
import java.util.*

private const val PROP_PORT = "fuel.hunter.port"
private const val PROP_DATA_FEED_REFRESH_INTERVAL = "fuel.hunter.dataFeedRefreshInterval"
private const val PROP_PROVIDER = "fuel.hunter.provider"
private const val PROP_DB_HOST = "fuel.hunter.database.host"
private const val PROP_DB_PORT = "fuel.hunter.database.port"

data class Configuration(
    val port: Int,
    val dataFeedRefreshInterval: Int,
    val provider: DocumentProvider,
    val database: String
) {
    override fun toString() = "" +
            "port: $port, " +
            "refresh interval: $dataFeedRefreshInterval, " +
            "provider: ${provider.javaClass.simpleName}, " +
            "database: $database"
}

private val defaultConfiguration = Configuration(
    port = 50051,
    dataFeedRefreshInterval = 5 * 60 * 60 * 1000,
    provider = OfflineDocumentProvider(),
    database = "mongodb://127.0.0.1:27017"
)

fun getConfiguration(path: String? = null): Configuration {
    path ?: return defaultConfiguration

    val classLoader = object {}.javaClass.classLoader

    val stream = classLoader.getResourceAsStream(path)
        ?: return defaultConfiguration

    return stream.use { inputStream ->
        val properties = Properties()
        properties.load(inputStream)

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

        val dbHost = System.getenv("DB_HOST")
            ?: properties.getProperty(PROP_DB_HOST)
            ?: "localhost"

        val dbPort = properties.getProperty(PROP_DB_PORT)?.toIntOrNull()
            ?: 27017

        Configuration(port, dataFeedRefreshInterval, provider, "mongodb://$dbHost:$dbPort")
    }
}