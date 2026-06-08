package net.lausi95.citygame.application.domain.model.game

import net.lausi95.citygame.application.domain.NotFoundDomainException

class GameNotFoundException(message: String) : NotFoundDomainException(message)

fun gameNotFound(gameId: GameId): Nothing {
    throw GameNotFoundException("Game not found: $gameId")
}