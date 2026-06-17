package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.agent.Agent
import org.springframework.data.domain.Page

@Schema(description = "Paginated collection of agents")
data class AgentCollection(

    @Schema(description = "Page of agent resources")
    @JsonUnwrapped
    val agents: Page<AgentResource>,

    @Schema(description = "Navigation links")
    val links: Map<String, String>
) {
    constructor(agents: Page<Agent>) : this(
        agents = agents.map { AgentResource(it) },
        links = mapOf(),
    )
}
