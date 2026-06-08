package net.lausi95.citygame.application.domain.sevice

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.gameNotFound
import net.lausi95.citygame.application.port.`in`.game.GetGameUseCase
import net.lausi95.citygame.application.port.out.GameRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service

@Service
class GetGameService(
    private val gameRepository: GameRepository,
) : GetGameUseCase {

    override fun getGame(
        gameId: GameId,
        tenant: Tenant
    ): Game {
        return gameRepository.findById(gameId, tenant) ?: gameNotFound(gameId)
    }
}