package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.port.`in`.game.UpdateGameUseCase
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
internal class UpdateGameService(
    private val gameRepository: GameRepository,
) : UpdateGameUseCase {

    @Transactional
    override fun updateGame(command: UpdateGameUseCase.Command, tenant: Tenant) {
        log.info { "Updating game..." }
        val game = gameRepository.get(command.gameId, tenant)

        command.title?.also {
            gameRepository.requireTitleAvailable(command.title, tenant)
            game.updateTitle(it)
        }

        command.endTime?.also {
            game.updateEndTime(it)
        }

        command.startTime?.also {
            game.updateStartTime(it)
        }

        command.map?.cornerA?.also {
            game.updateCornerA(it)
        }

        command.map?.cornerB?.also {
            game.updateCornerB(it)
        }

        command.map?.grid?.also {
            game.updateGrid(it)
        }

        gameRepository.save(game, tenant)

        log.info { "Game updated." }
    }
}