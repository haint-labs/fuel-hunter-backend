package fuel.hunter.rest

import kotlinx.serialization.Serializable

@Serializable
sealed class Update(val status: String) {
    @Serializable
    data class Success(val count: Int) : Update("Success")

    @Serializable
    data class Failure(val reason: String) : Update("Failure")
}