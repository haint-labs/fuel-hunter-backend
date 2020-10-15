package fuel.hunter.tools

import org.bson.*
import org.bson.conversions.Bson

fun bson(vararg pairs: Pair<String, Any>) = BsonDocument()
    .apply {
        for ((key, value) in pairs) {
            when (value) {
                is Int -> append(key, BsonInt32(value))
                is Float -> append(key, BsonDouble(value.toDouble()))
                is Double -> append(key, BsonDouble(value))
                is String -> append(key, BsonString(value))
                is Boolean -> append(key, BsonBoolean(value))
                is BsonValue -> append(key, value)
                else -> throw Error("Invalid bson type - ${value::javaClass}")
            }
        }
    }

fun geoNear(
    near: List<Float>,
    maxDistance: Float,
    key: String,
    distanceField: String = "distance",
    spherical: Boolean = true
): Bson {
    val coordinates = near
        .map(Float::toDouble)
        .map(::BsonDouble)

    val point = bson(
        "type" to "Point",
        "coordinates" to BsonArray(coordinates)
    )

    val params = bson(
        "near" to point,
        "key" to key,
        "maxDistance" to maxDistance,
        "distanceField" to distanceField,
        "spherical" to spherical,
    )

    return bson("\$geoNear" to params)
}

fun round(field: String, place: Int): BsonDocument {
    val params = BsonArray().apply {
        add(BsonString(field))
        add(BsonInt32(place))
    }

    return bson("\$round" to params)
}