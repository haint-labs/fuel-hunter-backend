package fuel.hunter.repo

import org.bson.types.ObjectId

data class Price2(
    val id: String = ObjectId.get().toHexString(),
    val stationId: String,
    val sessionId: String,
    val type: String,
    val price: Float
)