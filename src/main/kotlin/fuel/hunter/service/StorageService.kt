package fuel.hunter.service

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.repo.Price2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst

@ExperimentalCoroutinesApi
fun CoroutineScope.launchStorage(
    input: Flow<List<Price2>>,
    dbClient: MongoClient
) = launch {
    val collection = dbClient
        .getDatabase("fuel-hunter")
        .getCollection("prices", Price2::class.java)

    input
        .onStart { println("[STORAGE] Started...") }
        .onEach { chunk ->
            println("got new chunk ${chunk.size}")
            chunk
                .windowed(500, 500, true)
                .onEach {
                    val result = collection.insertMany(it).awaitFirst()

                    println("[STORAGE] Saved prices batch - amount: ${result.insertedIds.size}")
                }
        }
        .onCompletion { println("[STORAGE] Closed") }
        .collect()
}