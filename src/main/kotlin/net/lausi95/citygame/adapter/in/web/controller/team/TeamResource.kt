package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonProperty
import net.lausi95.citygame.application.domain.model.team.Team

data class TeamResource(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("name")
    val name: String,

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
