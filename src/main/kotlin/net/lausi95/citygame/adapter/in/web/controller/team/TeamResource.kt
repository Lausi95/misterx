package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.team.Team

@Schema(description = "Represents a team")
data class TeamResource(

    @Schema(description = "Unique identifier of the team")
    @JsonProperty("id")
    val id: String,

    @Schema(description = "Display name of the team")
    @JsonProperty("name")
    val name: String,

    @Schema(description = "Navigation links")
    @JsonProperty("links")
    val links: Map<String, String>,
) {
    constructor(team: Team) : this(
        id = team.id.value,
        name = team.name,
        links = mapOf(
            "self" to "/games/${team.gameId.value}/teams/${team.id.value}"
        )
    )
}
