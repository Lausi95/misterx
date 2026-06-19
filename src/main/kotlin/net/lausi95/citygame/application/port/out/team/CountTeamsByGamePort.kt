package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

interface CountTeamsByGamePort {

    fun countTeamsByGame(gameId: GameId, tenant: Tenant): Int
}
