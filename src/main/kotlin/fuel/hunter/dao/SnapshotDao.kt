package fuel.hunter.dao

import fuel.hunter.Snapshot
import fuel.hunter.SnapshotQuery

interface SnapshotDao {
    suspend fun getMatching(query: SnapshotQuery): List<Snapshot>
}

class InMemorySnapshotDao(private val items: List<Snapshot>) : SnapshotDao {
    override suspend fun getMatching(query: SnapshotQuery): List<Snapshot> {
        return items
    }
}