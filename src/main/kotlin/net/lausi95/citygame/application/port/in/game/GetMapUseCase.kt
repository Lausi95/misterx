package net.lausi95.citygame.application.port.`in`.game

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.common.Tenant

interface GetMapUseCase {

    fun getMap(gameId: GameId, tenant: Tenant): Map
}