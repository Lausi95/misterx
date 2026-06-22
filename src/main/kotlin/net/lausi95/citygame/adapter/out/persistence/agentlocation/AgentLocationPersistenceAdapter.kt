package net.lausi95.citygame.adapter.out.persistence.agentlocation

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.port.out.agentlocation.DeleteAgentLocationsPort
import net.lausi95.citygame.application.port.out.agentlocation.GetAgentLocationPort
import net.lausi95.citygame.application.port.out.agentlocation.SaveAgentLocationPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component

@Component
internal class AgentLocationPersistenceAdapter(
    private val agentLocationEntityRepository: AgentLocationEntityRepository,
    private val agentLocationCache: AgentLocationCache,
) : SaveAgentLocationPort, GetAgentLocationPort, DeleteAgentLocationsPort {

    override fun saveAgentLocation(
        agentLocation: AgentLocation,
        tenant: Tenant
    ) {
        agentLocationEntityRepository.save(AgentLocationEntity(agentLocation, tenant))
        agentLocationCache.putAgentLocation(agentLocation.agentId, agentLocation)
    }

    override fun getAgentLocation(agentId: AgentId): AgentLocation? = agentLocationCache.resolve(agentId) {
        agentLocationEntityRepository.findFirstByAgentIdOrderByTimestampDesc(agentId.value)?.toAgentLocation()
    }

    override fun deleteAgentLocations(agentId: AgentId, tenant: Tenant) {
        agentLocationEntityRepository.deleteByAgentIdAndTenant(agentId.value, tenant.value)
        agentLocationCache.evict(agentId)
    }
}
