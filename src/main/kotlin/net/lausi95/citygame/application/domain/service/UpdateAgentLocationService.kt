package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocationId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agentlocation.UpdateAgentLocationUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.SaveAgentLocationPort
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset

private val log = KotlinLogging.logger { }

@Component
class UpdateAgentLocationService(
    private val agentRepository: AgentRepository,
    private val saveAgentLocationPort: SaveAgentLocationPort,
    private val gameRepository: GameRepository,
) : UpdateAgentLocationUseCase {

    override fun updateAgentLocation(
        gameId: GameId,
        agentId: AgentId,
        geoLocation: GeoLocation,
        tenant: Tenant
    ) {
        log.info { "Updating agent location..." }

        gameRepository.requireExists(gameId, tenant)
        agentRepository.requireExists(agentId, tenant)

        val agentLocation = AgentLocation(
            AgentLocationId(),
            agentId,
            OffsetDateTime.now(ZoneOffset.UTC),
            geoLocation,
        )

        saveAgentLocationPort.saveAgentLocation(agentLocation, tenant)

        log.info { "Agent location updated" }
    }
}
