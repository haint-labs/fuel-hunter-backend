package fuel.hunter.scrapers

import java.net.URL

private object Resources

fun getResource(name: String): URL {
    val loader = Resources.javaClass.classLoader
    val resource = loader.getResource(name)
    return requireNotNull(resource)
}