package net.lausi95.citygame.application.port.out.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.common.Tenant

interface GetAgentFindingsPort {

    fun getFindingsByAgent(agentId: AgentId, tenant: Tenant): List<AgentFinding>
}
