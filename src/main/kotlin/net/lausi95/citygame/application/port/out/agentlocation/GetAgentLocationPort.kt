package net.lausi95.citygame.application.port.out.agentlocation

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation

interface GetAgentLocationPort {

    fun getAgentLocation(agentId: AgentId): AgentLocation?
}