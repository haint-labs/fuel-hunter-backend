package fuel.hunter.rest

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

inline fun <reified T, reified DTO : ItemsDTO<T>> Route.collection(
    path: String,
    dto: DTO
) = route("/$path") {
    get {
        call.respond(dto)
    }

    post {
        val response = runCatching { call.receive<DTO>() }
            .onSuccess {
                dto.items.clear()
                dto.items.addAll(it.items)
            }
            .fold(
                { Update.Success(it.items.size) },
                { Update.Failure(it.message ?: "Unknown reason") }
            )

        call.respond(response)
    }
}