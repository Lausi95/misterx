package net.lausi95.citygame.domain.game

class Game(
    private val _id: GameId,
    private var _title: GameTitle,
) {
    val id: GameId
        get() = _id

    val title: GameTitle
        get() = _title

    fun updateTitle(newTitle: GameTitle) {
        _title = newTitle
    }
}