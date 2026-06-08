package net.lausi95.citygame.application.domain.sevice

import net.lausi95.citygame.application.domain.model.game.*
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.port.`in`.game.CreateGameUseCase
import net.lausi95.citygame.application.port.out.GameRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateGameService(
    private val gameRepository: GameRepository
) : CreateGameUseCase {

    @Transactional
    override fun createGame(command: CreateGameUseCase.Command, tenant: Tenant): GameId {
        if (gameRepository.existsByTitle(command.title, tenant)) {
            gameTitleAlreadyExists(command.title)
        }

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

        return game.id
    }
}