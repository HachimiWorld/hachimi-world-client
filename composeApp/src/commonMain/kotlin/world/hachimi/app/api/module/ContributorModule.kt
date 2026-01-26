package world.hachimi.app.api.module

import kotlinx.serialization.Serializable
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.WebResult

class ContributorModule(
    private val client: ApiClient
) {
    @Serializable
    data class CheckResp(
        val isContributor: Boolean
    )

    suspend fun check(): WebResult<CheckResp> =
        client.get("/contributor/check")
}
