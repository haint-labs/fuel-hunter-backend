package fuel.hunter.rest

import com.mongodb.reactivestreams.client.MongoClient
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

fun CoroutineScope.launchRestService(dbClient: MongoClient) = launch {
    embeddedServer(Netty, port = 8000) {
        install(StatusPages) {
            exception<Throwable> {
                call.respond(mapOf("error" to (it.message ?: "ooups...")))
            }
        }

        install(ContentNegotiation) { json() }

        routing {
            collection<Station, StationsDTO>("stations", dbClient) { StationsDTO(it) }
            collection<Company, CompaniesDTO>("companies", dbClient) { CompaniesDTO(it) }
        }

    }.start(wait = true)
}