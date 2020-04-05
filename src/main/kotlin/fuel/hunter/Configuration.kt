package fuel.hunter

import fuel.hunter.scrapers.DocumentProvider
import fuel.hunter.scrapers.internal.OfflineDocumentProvider
import java.util.*

private const val PROP_PORT = "fuel.hunter.port"
private const val PROP_PROVIDER = "fuel.hunter.provider"

data class Configuration(
    val port: Int,
    val provider: DocumentProvider
)

private val defaultConfiguration = Configuration(
    port = 50051,
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

        val provider = properties
            .getProperty(PROP_PROVIDER)
            ?.let { providerClass ->
                classLoader.loadClass(providerClass)
                    .getDeclaredConstructor()
                    .newInstance() as DocumentProvider
            }
            ?: defaultConfiguration.provider

        Configuration(port, provider)
    }
}