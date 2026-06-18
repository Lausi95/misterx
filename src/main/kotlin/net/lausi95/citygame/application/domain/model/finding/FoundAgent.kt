package net.lausi95.citygame.application.domain.model.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId

/**
 * Projection of an agent a team has found, exposing only the agent's identity and display name.
 */
class FoundAgent(
    val agentId: AgentId,
    val name: String,
)
