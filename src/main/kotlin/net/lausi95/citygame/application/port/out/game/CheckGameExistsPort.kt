package net.lausi95.citygame.application.port.out.game

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

interface CheckGameExistsPort {

    fun gameExists(gameId: GameId, tenant: Tenant): Boolean

    fun assertGameExists(gameId: GameId, tenant: Tenant) {
        if (!gameExists(gameId, tenant)) {
            error("Game $gameId does not exist")
        }
    }
}
