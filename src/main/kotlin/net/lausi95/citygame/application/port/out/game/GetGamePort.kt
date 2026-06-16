package net.lausi95.citygame.application.port.out.game

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.gameNotFound
import net.lausi95.citygame.common.Tenant

interface GetGamePort {

    fun getGameOrNull(gameId: GameId, tenant: Tenant): Game?

    fun getGame(gameId: GameId, tenant: Tenant): Game {
        return getGameOrNull(gameId, tenant) ?: gameNotFound(gameId)
    }
}