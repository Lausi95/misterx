package net.lausi95.citygame.application.domain.sevice

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocationId
import net.lausi95.citygame.application.port.`in`.agentlocation.UpdateAgentLocationUseCase
import net.lausi95.citygame.application.port.out.agent.CheckAgentExistsPort
import net.lausi95.citygame.application.port.out.agentlocation.SaveAgentLocationPort
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class UpdateAgentLocationService(
    private val checkAgentExistsPort: CheckAgentExistsPort,
    private val saveAgentLocationPort: SaveAgentLocationPort,
) : UpdateAgentLocationUseCase {

    override fun updateAgentLocation(
        agentId: AgentId,
        geoLocation: GeoLocation,
        tenant: Tenant
    ) {
        checkAgentExistsPort.requireAgentExists(agentId, tenant)

        val agentLocation = AgentLocation(
            AgentLocationId(),
            agentId,
            ZonedDateTime.now(),
            geoLocation,
        )

        saveAgentLocationPort.saveAgentLocation(agentLocation, tenant)
    }
}
