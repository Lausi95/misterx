package net.lausi95.citygame.application.domain.model.finding

import net.lausi95.citygame.application.domain.UnprocessableDomainException
import net.lausi95.citygame.application.domain.model.agent.AgentId

class AgentNotFindableException(message: String) : UnprocessableDomainException(message)

fun agentNotFindable(agentId: AgentId): Nothing {
    throw AgentNotFindableException("Agent ${agentId.value} is not findable")
}
