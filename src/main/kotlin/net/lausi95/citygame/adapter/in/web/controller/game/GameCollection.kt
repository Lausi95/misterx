package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.game.Game
import org.springframework.data.domain.Page

@Schema(description = "Paginated collection of games")
data class GameCollection(
    @Schema(description = "Page of game resources")
    @JsonUnwrapped
    val games: Page<GameResource>,

    @Schema(description = "Navigation links")
    val links: Map<String, String>
) {
    constructor(games: Page<Game>) : this(
        games = games.map { GameResource(it) },
        links = mapOf(
            "self" to "/games"
        ),
    )
}
