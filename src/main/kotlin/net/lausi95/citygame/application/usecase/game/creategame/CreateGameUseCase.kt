package net.lausi95.citygame.application.usecase.game.creategame

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.domain.Tenant
import net.lausi95.citygame.domain.game.Game
import net.lausi95.citygame.domain.game.GameId
import net.lausi95.citygame.domain.game.GameRepository
import net.lausi95.citygame.domain.game.gameTitleAlreadyExists
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class CreateGameUseCase(
    private val gameRepository: GameRepository,
) {

    @Transactional
    operator fun invoke(command: CreateGameCommand, tenant: Tenant): CreateGameResult {
        if (gameRepository.existsByTitle(command.title, tenant)) {
            gameTitleAlreadyExists(command.title)
        }

        val game = Game(GameId.random(), command.title, command.startTime, command.endTime)
        gameRepository.save(game, tenant)

        log.info { "Game created" }

        return CreateGameResult(
            gameId = game.id
        )
    }
}
