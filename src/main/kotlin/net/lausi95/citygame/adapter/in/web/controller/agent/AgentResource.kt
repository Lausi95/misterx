package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import net.lausi95.citygame.application.domain.model.agent.Agent

data class AgentResource(
    @field:JsonProperty("id")
    val id: String,

    @field:JsonProperty("type")
    val type: Agent.Type,

    @field:JsonProperty("phoneNumber")
    val phoneNumber: String,

    @field:JsonProperty("firstName")
    val firstName: String,

    @field:JsonProperty("lastName")
    val lastName: String,

    @field:JsonProperty("alias")
    val alias: String,

    @field:JsonProperty("active")
    val active: Boolean,

    @field:JsonProperty("links")
    val links: Map<String, String>
) {
    constructor(agent: Agent) : this(
        id = agent.id.value,
        type = agent.type,
        phoneNumber = agent.phoneNumber,
        firstName = agent.firstname,
        lastName = agent.lastName,
        alias = agent.alias,
        active = agent.active,
        links = mapOf(
            "self" to "/games/${agent.gameId.value}/agents/${agent.id.value}"
        )
    )
}
