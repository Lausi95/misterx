package net.lausi95.citygame.application.port.out.agent

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

interface CountAgentsByGamePort {

    fun countAgentsByGame(gameId: GameId, tenant: Tenant): Int
}
