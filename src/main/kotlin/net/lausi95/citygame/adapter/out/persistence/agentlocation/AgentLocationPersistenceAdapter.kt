package net.lausi95.citygame.adapter.out.persistence.agentlocation

import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.port.out.agentlocation.SaveAgentLocationPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component

@Component
internal class AgentLocationPersistenceAdapter(
    private val agentLocationEntityRepository: AgentLocationEntityRepository,
) : SaveAgentLocationPort {

    override fun saveAgentLocation(
        agentLocation: AgentLocation,
        tenant: Tenant
    ) {
        agentLocationEntityRepository.save(AgentLocationEntity(agentLocation, tenant))
    }
}
