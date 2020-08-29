package fuel.hunter.service

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.Prices
import fuel.hunter.models.Price
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.coroutine

@ExperimentalCoroutinesApi
fun CoroutineScope.launchStorage(
    input: Flow<Prices>,
    dbClient: MongoClient
) = launch {
    val collection = dbClient
        .coroutine
        .getDatabase("fuel-hunter")
        .getCollection<Price>("prices")

    input
        .onStart { println("[STORAGE] Started...") }
        .onEach { chunk ->
            println("got new chunk ${chunk.size}")
            chunk
                .windowed(500, 500, true)
                .map { prices ->
                    val identified = prices.map(Price::withGeneratedId)
                    val result = collection.insertMany(identified)

                    println("[STORAGE] Saved prices batch - amount: ${result.insertedIds.size}")
                }
        }
        .onCompletion { println("[STORAGE] Closed") }
        .collect()
}

fun Price.withGeneratedId(): Price = toBuilder().setId(ObjectId.get().toHexString()).build()