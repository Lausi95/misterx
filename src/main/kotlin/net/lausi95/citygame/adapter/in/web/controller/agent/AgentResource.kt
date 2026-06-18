package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.agent.Agent

@Schema(description = "Represents a game agent (player)")
data class AgentResource(

    @Schema(description = "Unique identifier of the agent")
    @JsonProperty("id")
    val id: String,

    @Schema(description = "Agent type")
    @JsonProperty("type")
    val type: Agent.Type,

    @Schema(description = "Contact phone number")
    @JsonProperty("phoneNumber")
    val phoneNumber: String,

    @Schema(description = "Agent's first name")
    @JsonProperty("firstName")
    val firstName: String,

    @Schema(description = "Agent's last name")
    @JsonProperty("lastName")
    val lastName: String,

    @Schema(description = "In-game alias")
    @JsonProperty("alias")
    val alias: String,

    @Schema(description = "Whether the agent is currently active")
    @JsonProperty("active")
    val active: Boolean,

    @Schema(description = "Last known location, null if never reported")
    @JsonProperty("location")
    val location: AgentLocationResource?,

    @Schema(description = "Navigation links")
    @JsonProperty("links")
    val links: Map<String, String>,

    ) {
    constructor(agent: Agent) : this(
        id = agent.id.value,
        type = agent.type,
        phoneNumber = agent.phoneNumber,
        firstName = agent.firstName,
        lastName = agent.lastName,
        alias = agent.alias,
        active = agent.active,
        location = agent.location?.let { AgentLocationResource(it) },
        links = mapOf(
            "self" to "/games/${agent.gameId.value}/agents/${agent.id.value}"
        )
    )
}
