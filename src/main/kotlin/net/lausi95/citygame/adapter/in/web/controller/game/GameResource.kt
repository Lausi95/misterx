package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import net.lausi95.citygame.application.domain.model.game.Game
import java.time.OffsetDateTime

data class GameResource(

    @JsonProperty("id")
    val id: String,

    @JsonProperty("title")
    val title: String,

    @JsonProperty("startTime")
    val startTime: OffsetDateTime,

    @JsonProperty("endTime")
    val endTime: OffsetDateTime,

    @JsonProperty("teams")
    val teams: Int,

    @JsonProperty("agents")
    val agents: Int,

    @JsonProperty("links")
    val links: Map<String, String>
) {
    constructor(game: Game) : this(
        id = game.id.value,
        title = game.title.value,
        startTime = game.startTime,
        endTime = game.endTime,
        teams = 0,
        agents = 0,
        links = mapOf(
            "self" to "/games/${game.id.value}",
            "map" to "/games/${game.id.value}/map",
            "agents" to "/games/${game.id.value}/agents",
            "teams" to "/games/${game.id.value}/teams",
        )
    )
}
