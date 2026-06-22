package net.lausi95.citygame.application.port.out.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.common.Tenant

interface DeleteAgentFindingsPort {

    fun deleteAgentFindings(agentId: AgentId, tenant: Tenant)
}
