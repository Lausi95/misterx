package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.port.`in`.game.UpdateGameUseCase
import net.lausi95.citygame.application.port.out.game.CheckGameWithTitleDoesNotExistPort
import net.lausi95.citygame.application.port.out.game.GetGamePort
import net.lausi95.citygame.application.port.out.game.SaveGamePort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
internal class UpdateGameService(
    private val getGamePort: GetGamePort,
    private val saveGamePort: SaveGamePort,
    private val gameWithTitleDoesNotExistPort: CheckGameWithTitleDoesNotExistPort,
) : UpdateGameUseCase {

    @Transactional
    override fun updateGame(command: UpdateGameUseCase.Command, tenant: Tenant) {
        log.info { "Updating game..." }
        val game = getGamePort.getGame(command.gameId, tenant)

        command.title?.also {
            gameWithTitleDoesNotExistPort.assertGameWithTitleDoesNotExist(command.title, tenant)
            game.updateTitle(it)
        }

        command.startTime?.also {
            game.updateStartTime(it)
        }

        command.endTime?.also {
            game.updateEndTime(it)
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

        saveGamePort.saveGame(game, tenant)

        log.info { "Game updated." }
    }
}