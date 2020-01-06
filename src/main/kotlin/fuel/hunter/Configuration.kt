package fuel.hunter

import java.util.*

private const val PROP_PORT = "fuel.hunter.port"

data class Configuration(val port: Int)

private val defaultConfiguration = Configuration(
    port = 50051
)

fun getConfiguration(path: String? = null): Configuration {
    path ?: return defaultConfiguration

    val stream = object {}.javaClass.classLoader.getResourceAsStream(path)
        ?: return defaultConfiguration

    return stream.use {
        val properties = Properties()
        properties.load(it)

        val port = properties
            .getProperty(PROP_PORT, "${defaultConfiguration.port}")
            .toInt()

        Configuration(port)
    }
}