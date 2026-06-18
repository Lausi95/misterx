package net.lausi95.citygame.application.domain.model.team

import net.lausi95.citygame.application.domain.model.game.GameId
import java.util.*

@JvmInline
value class TeamId(val value: String = UUID.randomUUID().toString()) {
    override fun toString(): String = value
}

class Team(
    val id: TeamId,
    private val _gameId: GameId,
    private var _name: String,
) {
    val gameId: GameId
        get() = _gameId

    val name: String
        get() = _name

    fun updateName(name: String) {
        _name = name
    }
}
