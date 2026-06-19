package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.game.GameSummary
import java.time.OffsetDateTime

@Schema(description = "Represents a game")
data class GameResource(

    @Schema(description = "Unique identifier of the game")
    @JsonProperty("id")
    val id: String,

    @Schema(description = "Display title of the game")
    @JsonProperty("title")
    val title: String,

    @Schema(description = "Scheduled start time of the game")
    @JsonProperty("startTime")
    val startTime: OffsetDateTime,

    @Schema(description = "Scheduled end time of the game")
    @JsonProperty("endTime")
    val endTime: OffsetDateTime,

    @Schema(description = "Number of teams in this game")
    @JsonProperty("teams")
    val teams: Int,

    @Schema(description = "Number of agents in this game")
    @JsonProperty("agents")
    val agents: Int,

    @Schema(description = "Navigation links")
    @JsonProperty("links")
    val links: Map<String, String>
) {
    constructor(summary: GameSummary) : this(
        id = summary.game.id.value,
        title = summary.game.title.value,
        startTime = summary.game.startTime,
        endTime = summary.game.endTime,
        teams = summary.teamsCount,
        agents = summary.agentsCount,
        links = mapOf(
            "self" to "/games/${summary.game.id.value}",
            "map" to "/games/${summary.game.id.value}/map",
            "agents" to "/games/${summary.game.id.value}/agents",
            "teams" to "/games/${summary.game.id.value}/teams",
        )
    )
}
