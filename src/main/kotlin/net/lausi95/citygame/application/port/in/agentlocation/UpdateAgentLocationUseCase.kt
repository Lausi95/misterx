package net.lausi95.citygame.application.port.`in`.agentlocation

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant

interface UpdateAgentLocationUseCase {

    fun updateAgentLocation(
        gameId: GameId,
        agentId: AgentId,
        geoLocation: GeoLocation,
        tenant: Tenant,
    )
}
