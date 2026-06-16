package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonUnwrapped
import net.lausi95.citygame.application.domain.model.agent.Agent
import org.springframework.data.domain.Page

data class AgentCollection(
    @JsonUnwrapped
    val agents: Page<AgentResource>,
    val links: Map<String, String>
) {
    constructor(agents: Page<Agent>) : this(
        agents = agents.map { AgentResource(it) },
        links = mapOf(),
    )
}