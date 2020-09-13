package fuel.hunter.repo

import org.bson.types.ObjectId

data class Session(
    val id: String = ObjectId.get().toHexString(),
    val timestamp: Long
)