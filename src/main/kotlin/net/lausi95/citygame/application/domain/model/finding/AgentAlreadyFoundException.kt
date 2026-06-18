package net.lausi95.citygame.application.domain.model.finding

import net.lausi95.citygame.application.domain.ConflictDomainException
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.team.TeamId

class AgentAlreadyFoundException(message: String) : ConflictDomainException(message)

fun agentAlreadyFound(teamId: TeamId, agentId: AgentId): Nothing {
    throw AgentAlreadyFoundException("Team ${teamId.value} has already found agent ${agentId.value}")
}
