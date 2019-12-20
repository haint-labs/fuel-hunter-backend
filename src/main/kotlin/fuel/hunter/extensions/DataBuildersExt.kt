package fuel.hunter.extensions

import fuel.hunter.Snapshot
import fuel.hunter.SnapshotResponse

inline fun snapshot(init: Snapshot.Builder.() -> Unit): Snapshot {
    return Snapshot.newBuilder().apply(init).build()
}

inline fun snapshotResponse(init: SnapshotResponse.Builder.() -> Unit): SnapshotResponse {
    return SnapshotResponse.newBuilder().apply(init).build()
}