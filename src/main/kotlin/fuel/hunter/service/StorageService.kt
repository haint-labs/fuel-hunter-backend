package fuel.hunter.service

import com.mongodb.reactivestreams.client.MongoClient
import fuel.hunter.models.Price
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.coroutine

@ExperimentalCoroutinesApi
fun CoroutineScope.launchStorage(
    input: ReceiveChannel<Price>,
    dbClient: MongoClient
) = launch {
    val collection = dbClient
        .coroutine
        .getDatabase("fuel-hunter")
        .getCollection<Price>("prices")

    input
        .consumeAsFlow()
        .onStart { println("[STORAGE] Started...") }
        .onEach {
            val p = it.toBuilder()
                .setId(ObjectId.get().toHexString())
                .build()

            collection.insertOne(p)
            println("[STORAGE] Saved snapshot - name: ${it.name}, type: ${it.type}, price: ${it.price}")
        }
        .onCompletion { println("[STORAGE] Closed") }
        .collect()
}