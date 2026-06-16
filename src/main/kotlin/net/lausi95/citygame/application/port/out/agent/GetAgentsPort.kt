package net.lausi95.citygame.application.port.out.agent

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetAgentsPort {

    fun getAgents(pageable: Pageable, gameId: GameId, tenant: Tenant): Page<Agent>
}
