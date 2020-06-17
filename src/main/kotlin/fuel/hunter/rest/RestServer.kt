package fuel.hunter.rest

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun CoroutineScope.launchRestService(
    stations: MutableList<Station>,
    companies: MutableList<Company>
) = launch {
    embeddedServer(Netty, port = 8000) {
        install(StatusPages) {
            exception<Throwable> {
                call.respond(mapOf("error" to (it.message ?: "ooups...")))
            }
        }
        install(ContentNegotiation) { json() }

        routing {
            collection("/stations", StationsDTO(stations))
            collection("/companies", CompaniesDTO(companies))
        }

    }.start(wait = true)
}