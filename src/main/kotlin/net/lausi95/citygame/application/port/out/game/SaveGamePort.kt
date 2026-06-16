package net.lausi95.citygame.application.port.out.game

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.common.Tenant

interface SaveGamePort {

    fun saveGame(game: Game, tenant: Tenant)
}