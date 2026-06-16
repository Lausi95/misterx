package net.lausi95.citygame.application.domain.sevice

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.GetAgentsUseCase
import net.lausi95.citygame.application.port.out.agent.GetAgentsPort
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class GetAgentsService(
    private val getAgentsPort: GetAgentsPort
) : GetAgentsUseCase {

    override fun getAgents(
        gameId: GameId,
        pageable: Pageable,
        tenant: Tenant
    ): Page<Agent> {
        return getAgentsPort.getAgents(pageable, gameId, tenant)
    }
}