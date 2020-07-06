package fuel.hunter.extensions

import fuel.hunter.models.Price

inline fun price(init: Price.Builder.() -> Unit): Price {
    return Price.newBuilder().apply(init).build()
}

inline fun priceResponse(init: Price.Response.Builder.() -> Unit): Price.Response {
    return Price.Response.newBuilder().apply(init).build()
}