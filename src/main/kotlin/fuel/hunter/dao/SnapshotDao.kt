package fuel.hunter.dao

import fuel.hunter.models.Snapshot

interface SnapshotDao {
    suspend fun getMatching(query: Snapshot.Query): List<Snapshot>
}

class InMemorySnapshotDao(private val items: List<Snapshot>) : SnapshotDao {
    override suspend fun getMatching(query: Snapshot.Query): List<Snapshot> {
        return items
    }
}