package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.game.MapId
import net.lausi95.citygame.application.port.`in`.game.CreateGameUseCase
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class CreateGameService(
    private val gameRepository: GameRepository,
) : CreateGameUseCase {

    @Transactional
    override fun createGame(command: CreateGameUseCase.Command, tenant: Tenant): GameId {
        log.info { "Creating new game..." }

        gameRepository.requireTitleAvailable(command.title, tenant)

        val game = Game(
            GameId(),
            command.title,
            command.startTime,
            command.endTime,
            Map(
                MapId(),
                command.map.cornerA,
                command.map.cornerB,
                command.map.grid,
            )
        )

        gameRepository.save(game, tenant)

        log.info { "Game created." }

        return game.id
    }
}