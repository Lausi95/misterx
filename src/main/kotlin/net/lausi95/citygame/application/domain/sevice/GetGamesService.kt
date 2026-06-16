package net.lausi95.citygame.application.domain.sevice

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.port.`in`.game.GetGamesUseCase
import net.lausi95.citygame.application.port.out.game.GetGamesPort
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class GetGamesService(
    private val getGamesPort: GetGamesPort
) : GetGamesUseCase {

    override fun getGames(
        pageable: Pageable,
        tenant: Tenant
    ): Page<Game> {
        return getGamesPort.getGames(pageable, tenant)
    }
}