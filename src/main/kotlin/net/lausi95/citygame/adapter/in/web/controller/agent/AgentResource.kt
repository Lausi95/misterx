package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import net.lausi95.citygame.application.domain.model.agent.Agent

data class AgentResource(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("type")
    val type: Agent.Type,

    @JsonProperty("phoneNumber")
    val phoneNumber: String,

    @JsonProperty("firstName")
    val firstName: String,

    @JsonProperty("lastName")
    val lastName: String,

    @JsonProperty("alias")
    val alias: String,

    @JsonProperty("active")
    val active: Boolean,

    @JsonProperty("location")
    val location: AgentLocationResource?,

    @JsonProperty("links")
    val links: Map<String, String>,

    ) {
    constructor(agent: Agent) : this(
        id = agent.id.value,
        type = agent.type,
        phoneNumber = agent.phoneNumber,
        firstName = agent.firstname,
        lastName = agent.lastName,
        alias = agent.alias,
        active = agent.active,
        location = agent.location?.let { AgentLocationResource(it) },
        links = mapOf(
            "self" to "/games/${agent.gameId.value}/agents/${agent.id.value}"
        )
    )
}
