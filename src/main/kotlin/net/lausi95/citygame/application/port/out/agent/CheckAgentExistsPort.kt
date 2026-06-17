package net.lausi95.citygame.application.port.out.agent

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.common.Tenant

interface CheckAgentExistsPort {

    fun agentExists(agentId: AgentId, tenant: Tenant): Boolean

    fun requireAgentExists(agentId: AgentId, tenant: Tenant) =
        require(agentExists(agentId, tenant)) { "Agent does not exist." }
}