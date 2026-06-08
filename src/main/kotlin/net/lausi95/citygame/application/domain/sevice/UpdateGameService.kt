package net.lausi95.citygame.application.domain.sevice

import net.lausi95.citygame.application.domain.model.game.gameNotFound
import net.lausi95.citygame.application.domain.model.game.gameTitleAlreadyExists
import net.lausi95.citygame.application.port.`in`.game.UpdateGameUseCase
import net.lausi95.citygame.application.port.out.GameRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class UpdateGameService(
    private val gameRepository: GameRepository
) : UpdateGameUseCase {

    @Transactional
    override fun updateGame(command: UpdateGameUseCase.Command, tenant: Tenant) {
        val game = gameRepository.findById(command.gameId, tenant) ?: gameNotFound(command.gameId)

        command.title?.also {
            if (gameRepository.existsByTitle(it, tenant)) {
                gameTitleAlreadyExists(it)
            }
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

        gameRepository.save(game, tenant)
    }
}