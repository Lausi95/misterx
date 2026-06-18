package net.lausi95.citygame.application.domain.model.game

import net.lausi95.citygame.application.domain.UnprocessableDomainException

class GameNotActiveException(message: String) : UnprocessableDomainException(message)

fun gameNotActive(gameId: GameId): Nothing {
    throw GameNotActiveException("Game ${gameId.value} is not currently active")
}
