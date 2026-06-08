package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import net.lausi95.citygame.application.domain.model.game.Game
import java.time.OffsetDateTime

data class GameResource(

    @field:JsonProperty("id")
    val id: String,

    @field:JsonProperty("title")
    val title: String,

    @field:JsonProperty("startTime")
    val startTime: OffsetDateTime,

    @field:JsonProperty("endTime")
    val endTime: OffsetDateTime,

    @field:JsonProperty("teams")
    val teams: Int,

    @field:JsonProperty("agents")
    val agents: Int,

    @field:JsonProperty("links")
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
            "map" to "/games/${game.id.value}/map"
        )
    )
}
