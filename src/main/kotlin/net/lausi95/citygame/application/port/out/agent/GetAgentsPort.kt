package net.lausi95.citygame.application.port.out.agent

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

interface GetAgentsPort {

    fun getAgentsForGame(gameId: GameId, tenant: Tenant): List<Agent>
}
