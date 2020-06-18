package fuel.hunter.rest

import com.mongodb.reactivestreams.client.MongoClient
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.litote.kmongo.coroutine.coroutine

inline fun <reified T : Any, reified DTO : ItemsDTO<T>> Route.collection(
    name: String,
    dbClient: MongoClient,
    crossinline dtoFactory: (items: List<T>) -> ItemsDTO<T>
) = route("/$name") {

    val collection = dbClient
        .coroutine
        .getDatabase("fuel-hunter")
        .getCollection<T>(name)

    get {
        val dto = dtoFactory(collection.find().toList())
        call.respond(dto)
    }

    post {
        val response = runCatching { call.receive<DTO>() }
            .mapCatching {
                collection.deleteMany()

                collection
                    .insertMany(it.items)
                    .insertedIds
                    .size
            }
            .fold(
                { Update.Success(it) },
                { Update.Failure(it.message ?: "Unknown reason") }
            )

        call.respond(response)
    }
}