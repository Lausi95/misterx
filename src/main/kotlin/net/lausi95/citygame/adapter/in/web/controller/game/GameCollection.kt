package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonUnwrapped
import net.lausi95.citygame.application.domain.model.game.Game
import org.springframework.data.domain.Page

data class GameCollection(
    @JsonUnwrapped
    val games: Page<GameResource>,
    val links: Map<String, String>
) {
    constructor(games: Page<Game>) : this(
        games = games.map { GameResource(it) },
        links = mapOf(
            "self" to "/games"
        ),
    )
}
