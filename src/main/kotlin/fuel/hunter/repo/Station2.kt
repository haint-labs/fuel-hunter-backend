package fuel.hunter.repo

import fuel.hunter.models.Station

data class Point(
    val coordinates: List<Float>,
    val type: String = "Point"
)

data class Station2(
    val id: String,
    val company: String,
    val location: Point,
    val address: String,
    val city: String,
    val name: String
)

fun Station.toEntity(): Station2 =
    Station2(id, company, Point(listOf(longitude, latitude)), address, city, name)

fun Station2.fromEntity(): Station =
    Station.newBuilder()
        .setId(id)
        .setCompany(company)
        .setLongitude(location.coordinates.first())
        .setLatitude(location.coordinates.last())
        .setAddress(address)
        .setCity(city)
        .setName(name)
        .build()