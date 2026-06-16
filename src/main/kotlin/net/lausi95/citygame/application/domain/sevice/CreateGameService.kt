package net.lausi95.citygame.application.domain.sevice

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.game.MapId
import net.lausi95.citygame.application.port.`in`.game.CreateGameUseCase
import net.lausi95.citygame.application.port.out.game.CheckGameWithTitleDoesNotExistPort
import net.lausi95.citygame.application.port.out.game.SaveGamePort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateGameService(
    private val gameWithTitleDoesNotExistPort: CheckGameWithTitleDoesNotExistPort,
    private val saveGamePort: SaveGamePort,
) : CreateGameUseCase {

    @Transactional
    override fun createGame(command: CreateGameUseCase.Command, tenant: Tenant): GameId {
        gameWithTitleDoesNotExistPort.assertGameWithTitleDoesNotExist(command.title, tenant)

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

        saveGamePort.saveGame(game, tenant)

        return game.id
    }
}