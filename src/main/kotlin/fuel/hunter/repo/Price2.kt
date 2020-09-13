package fuel.hunter.repo

import fuel.hunter.models.Price
import org.bson.types.ObjectId

data class Price2(
    val id: String,
    val stationId: String,
    val type: String,
    val price: Float
)

fun Price.toEntity(): Price2 =
    Price2(ObjectId.get().toHexString(), stationId, type.toString(), price)