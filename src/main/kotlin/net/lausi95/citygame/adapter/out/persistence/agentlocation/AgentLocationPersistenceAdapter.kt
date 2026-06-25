package net.lausi95.citygame.adapter.out.persistence.agentlocation

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component

@Component
internal class AgentLocationPersistenceAdapter(
    private val agentLocationEntityJpaRepository: AgentLocationEntityJpaRepository,
    private val agentLocationCache: AgentLocationCache,
) : AgentLocationRepository {

    override fun save(
        agentLocation: AgentLocation,
        tenant: Tenant
    ) {
        agentLocationEntityJpaRepository.save(AgentLocationEntity(agentLocation, tenant))
        agentLocationCache.putAgentLocation(agentLocation.agentId, agentLocation)
    }

    override fun latest(agentId: AgentId): AgentLocation? = agentLocationCache.resolve(agentId) {
        agentLocationEntityJpaRepository.findFirstByAgentIdOrderByTimestampDesc(agentId.value)?.toAgentLocation()
    }

    override fun deleteByAgent(agentId: AgentId, tenant: Tenant) {
        agentLocationEntityJpaRepository.deleteByAgentIdAndTenant(agentId.value, tenant.value)
        agentLocationCache.evict(agentId)
    }
}
