package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.finding.FindingTeam

@Schema(description = "A team that has found this agent, exposing only its identity and name")
data class FindingTeamResource(

    @Schema(description = "Unique identifier of the team")
    @JsonProperty("id")
    val id: String,

    @Schema(description = "Display name of the team")
    @JsonProperty("name")
    val name: String,
) {
    constructor(findingTeam: FindingTeam) : this(
        id = findingTeam.teamId.value,
        name = findingTeam.name,
    )
}
