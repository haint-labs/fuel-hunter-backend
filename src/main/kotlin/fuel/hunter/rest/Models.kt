package fuel.hunter.rest

import kotlinx.serialization.Serializable

interface ItemsDTO<T> {
    val items: List<T>
}

@Serializable
data class Company(
    val name: String,
    val order: Int,
    val hidden: Boolean,
    val description: Description,
    val homepage: String,
    val logo: Logo,
    val largeLogo: Logo,
    val mapLogo: Logo,
    val mapGrayLogo: Logo
)

@Serializable
data class Description(
    val en: String,
    val lv: String,
    val ru: String,
    val lg: String
)

@Serializable
data class Logo(
    val x2: String,
    val x3: String
)

@Serializable
data class CompaniesDTO(override val items: List<Company>) : ItemsDTO<Company>

@Serializable
data class Station(
    val id: Long,
    val company: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
    val name: String
)

@Serializable
data class StationsDTO(override val items: List<Station>) : ItemsDTO<Station>