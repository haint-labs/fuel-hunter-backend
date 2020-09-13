package fuel.hunter.service

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.models.Price
import fuel.hunter.repo.Price2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.coroutine

@ExperimentalCoroutinesApi
fun CoroutineScope.launchStorage(
    input: Flow<List<Price2>>,
    dbClient: MongoClient
) = launch {
    val collection = dbClient
        .coroutine
        .getDatabase("fuel-hunter")
        .getCollection<Price2>("prices")

    input
        .onStart { println("[STORAGE] Started...") }
        .onEach { chunk ->
            println("got new chunk ${chunk.size}")
            chunk
                .windowed(500, 500, true)
                .onEach {
                    val result = collection.insertMany(it)

                    println("[STORAGE] Saved prices batch - amount: ${result.insertedIds.size}")
                }
        }
        .onCompletion { println("[STORAGE] Closed") }
        .collect()
}

fun Price.withGeneratedId(): Price = toBuilder().setId(ObjectId.get().toHexString()).build()