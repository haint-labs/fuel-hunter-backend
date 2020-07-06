package fuel.hunter.dao

import fuel.hunter.models.Price

interface SnapshotDao {
    suspend fun getMatching(query: Price.Query): List<Price>
}

class InMemorySnapshotDao(private val items: List<Price>) : SnapshotDao {
    override suspend fun getMatching(query: Price.Query): List<Price> {
        return items
    }
}