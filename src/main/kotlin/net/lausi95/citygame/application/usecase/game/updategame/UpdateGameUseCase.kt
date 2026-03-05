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
        if (gameRepository.existsByTitle(command.title, tenant)) {
            gameTitleAlreadyExists(command.title)
        }

        val game = gameRepository.findById(command.gameId, tenant) ?: gameNotFound(command.gameId)
        game.updateTitle(command.title)

        gameRepository.save(game, tenant)
    }
}
