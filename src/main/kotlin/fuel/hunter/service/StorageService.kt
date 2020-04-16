package fuel.hunter.service

import fuel.hunter.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
fun CoroutineScope.launchStorage(
    input: ReceiveChannel<Snapshot>,
    items: MutableList<Snapshot>,
    limit: Int = 10000
) = launch {
    println("[STORAGE] Started...")

    var index = 0

    input.consumeEach {
        if (items.size < limit) {
            items.add(it)
        } else {
            items[index] = it
        }

        println("[STORAGE] Saved snapshot - [index: $index] ${it.name}")
        index = (index + 1) % limit
    }

    println("[STORAGE] Closed")
}