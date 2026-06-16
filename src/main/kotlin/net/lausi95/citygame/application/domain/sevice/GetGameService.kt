package net.lausi95.citygame.application.domain.sevice

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.game.GetGameUseCase
import net.lausi95.citygame.application.port.out.game.GetGamePort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service

@Service
class GetGameService(
    private val getGamePort: GetGamePort
) : GetGameUseCase {

    override fun getGame(
        gameId: GameId,
        tenant: Tenant
    ): Game {
        return getGamePort.getGame(gameId, tenant)
    }
}