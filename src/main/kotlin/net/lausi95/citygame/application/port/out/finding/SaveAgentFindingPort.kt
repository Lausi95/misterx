package net.lausi95.citygame.application.port.out.finding

import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.common.Tenant

interface SaveAgentFindingPort {

    fun saveAgentFinding(agentFinding: AgentFinding, tenant: Tenant)
}
