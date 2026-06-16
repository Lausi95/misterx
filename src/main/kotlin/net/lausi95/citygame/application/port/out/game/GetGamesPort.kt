package net.lausi95.citygame.application.port.out.game

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetGamesPort {

    fun getGames(pageable: Pageable, tenant: Tenant): Page<Game>
}