package net.lausi95.citygame.application.port.out.agent

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.common.Tenant

interface GetAgentPort {

    fun getAgentOrNull(agentId: AgentId, tenant: Tenant): Agent?

    fun getAgent(agentId: AgentId, tenant: Tenant): Agent {
        return getAgentOrNull(agentId, tenant) ?: error("Agent not found")
    }

    fun getAgentsByIds(agentIds: Collection<AgentId>, tenant: Tenant): List<Agent>
}