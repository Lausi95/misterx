package net.lausi95.citygame.application.port.`in`.game

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

interface GetGameUseCase {

    fun getGame(gameId: GameId, tenant: Tenant): Game
}
