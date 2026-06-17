package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocationId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agentlocation.UpdateAgentLocationUseCase
import net.lausi95.citygame.application.port.out.agent.CheckAgentExistsPort
import net.lausi95.citygame.application.port.out.agentlocation.SaveAgentLocationPort
import net.lausi95.citygame.application.port.out.game.CheckGameExistsPort
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

private val log = KotlinLogging.logger { }

@Component
class UpdateAgentLocationService(
    private val checkAgentExistsPort: CheckAgentExistsPort,
    private val saveAgentLocationPort: SaveAgentLocationPort,
    private val checkGameExistsPort: CheckGameExistsPort,
) : UpdateAgentLocationUseCase {

    override fun updateAgentLocation(
        gameId: GameId,
        agentId: AgentId,
        geoLocation: GeoLocation,
        tenant: Tenant
    ) {
        log.info { "Updating agent location..." }

        checkGameExistsPort.requireGameExists(gameId, tenant)
        checkAgentExistsPort.requireAgentExists(agentId, tenant)

        val agentLocation = AgentLocation(
            AgentLocationId(),
            agentId,
            ZonedDateTime.now(),
            geoLocation,
        )

        saveAgentLocationPort.saveAgentLocation(agentLocation, tenant)

        log.info { "Agent location updated" }
    }
}
