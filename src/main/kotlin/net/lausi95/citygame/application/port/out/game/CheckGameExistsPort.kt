package net.lausi95.citygame.application.port.out.game

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

interface CheckGameExistsPort {

    fun gameExists(gameId: GameId, tenant: Tenant): Boolean

    fun requireGameExists(gameId: GameId, tenant: Tenant) =
        require(gameExists(gameId, tenant)) { "Game with ID '${gameId.value}' does not exist" }
}
