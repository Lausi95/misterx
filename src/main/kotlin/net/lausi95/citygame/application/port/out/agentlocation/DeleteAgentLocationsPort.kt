package net.lausi95.citygame.application.port.out.agentlocation

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.common.Tenant

interface DeleteAgentLocationsPort {

    fun deleteAgentLocations(agentId: AgentId, tenant: Tenant)
}
