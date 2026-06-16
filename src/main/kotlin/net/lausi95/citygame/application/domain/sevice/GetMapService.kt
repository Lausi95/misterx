package net.lausi95.citygame.application.domain.sevice

import jakarta.transaction.Transactional
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.port.`in`.game.GetMapUseCase
import net.lausi95.citygame.application.port.out.game.GetGamePort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service

@Service
class GetMapService(
    private val getGamePort: GetGamePort,
) : GetMapUseCase {

    @Transactional
    override fun getMap(gameId: GameId, tenant: Tenant): Map {
        return getGamePort.getGame(gameId, tenant).map
    }
}
