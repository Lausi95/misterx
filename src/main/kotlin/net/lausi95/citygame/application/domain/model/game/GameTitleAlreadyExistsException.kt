package net.lausi95.citygame.application.domain.model.game

import net.lausi95.citygame.application.domain.DomainException

class GameTitleAlreadyExistsException(message: String) : DomainException(message)

fun gameTitleAlreadyExists(title: GameTitle): Nothing {
    throw GameTitleAlreadyExistsException("Game title already exist: $title")
}