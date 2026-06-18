package net.lausi95.citygame.application.domain.model.agent

import net.lausi95.citygame.application.domain.NotFoundDomainException

class AgentNotFoundException(message: String) : NotFoundDomainException(message)

fun agentNotFound(agentId: AgentId): Nothing {
    throw AgentNotFoundException("Agent not found: ${agentId.value}")
}
