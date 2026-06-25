package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.port.`in`.game.GetMapUseCase
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class GetMapService(
    private val gameRepository: GameRepository,
) : GetMapUseCase {

    @Transactional
    override fun getMap(gameId: GameId, tenant: Tenant): Map {
        log.info { "Fetching map..." }
        val map = gameRepository.get(gameId, tenant).map
        log.info { "Map fetched." }
        return map
    }
}
