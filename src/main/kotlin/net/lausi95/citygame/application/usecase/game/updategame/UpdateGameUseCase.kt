package net.lausi95.citygame.application.usecase.game.updategame

import net.lausi95.citygame.domain.Tenant
import net.lausi95.citygame.domain.game.GameRepository
import net.lausi95.citygame.domain.game.gameNotFound
import net.lausi95.citygame.domain.game.gameTitleAlreadyExists
import org.springframework.stereotype.Service

@Service
class UpdateGameUseCase(
    private val gameRepository: GameRepository
) {

    operator fun invoke(command: UpdateGameCommand, tenant: Tenant) {
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

        gameRepository.save(game, tenant)
    }
}
