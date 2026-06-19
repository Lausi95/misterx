package net.lausi95.citygame.application.port.`in`.game

import net.lausi95.citygame.application.domain.model.game.GameSummary
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetGamesUseCase {

    fun getGames(pageable: Pageable, tenant: Tenant): Page<GameSummary>
}
