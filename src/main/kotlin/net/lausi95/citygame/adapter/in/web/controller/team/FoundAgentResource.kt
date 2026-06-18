package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.finding.FoundAgent

@Schema(description = "An agent a team has found, exposing only its identity and alias")
data class FoundAgentResource(

    @Schema(description = "Unique identifier of the found agent")
    @JsonProperty("id")
    val id: String,

    @Schema(description = "In-game alias of the found agent")
    @JsonProperty("name")
    val name: String,
) {
    constructor(foundAgent: FoundAgent) : this(
        id = foundAgent.agentId.value,
        name = foundAgent.name,
    )
}
