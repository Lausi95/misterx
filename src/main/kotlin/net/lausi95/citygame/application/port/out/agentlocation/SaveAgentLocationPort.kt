package net.lausi95.citygame.application.port.out.agentlocation

import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.common.Tenant

interface SaveAgentLocationPort {

    fun saveAgentLocation(agentLocation: AgentLocation, tenant: Tenant)
}