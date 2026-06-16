package net.lausi95.citygame.application.domain.sevice

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.port.`in`.agent.GetAgentUseCase
import net.lausi95.citygame.application.port.out.agent.GetAgentPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component

@Component
class GetAgentService(
    private val getAgentPort: GetAgentPort,
) : GetAgentUseCase {

    override fun getAgent(
        agentId: AgentId,
        tenant: Tenant
    ): Agent {
        return getAgentPort.getAgent(agentId, tenant)
    }
}