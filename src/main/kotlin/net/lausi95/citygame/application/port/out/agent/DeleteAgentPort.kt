package net.lausi95.citygame.application.port.out.agent

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.common.Tenant

interface DeleteAgentPort {

    fun deleteAgent(agentId: AgentId, tenant: Tenant)
}
