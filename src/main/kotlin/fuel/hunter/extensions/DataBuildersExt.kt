package fuel.hunter.extensions

import fuel.hunter.models.Snapshot

inline fun snapshot(init: Snapshot.Builder.() -> Unit): Snapshot {
    return Snapshot.newBuilder().apply(init).build()
}

inline fun snapshotResponse(init: Snapshot.Response.Builder.() -> Unit): Snapshot.Response {
    return Snapshot.Response.newBuilder().apply(init).build()
}