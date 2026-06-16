package net.lausi95.citygame.application.port.`in`.agent

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.common.Tenant

interface GetAgentUseCase {

    fun getAgent(agentId: AgentId, tenant: Tenant): Agent
}
