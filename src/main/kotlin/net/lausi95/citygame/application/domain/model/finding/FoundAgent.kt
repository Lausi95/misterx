package net.lausi95.citygame.application.domain.model.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId
import java.time.OffsetDateTime

/**
 * Projection of an agent a team has found, exposing the agent's identity and display name
 * together with when the team found it.
 */
class FoundAgent(
    val agentId: AgentId,
    val name: String,
    val foundAt: OffsetDateTime,
)
