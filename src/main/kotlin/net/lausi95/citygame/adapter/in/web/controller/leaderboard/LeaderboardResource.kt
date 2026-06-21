package net.lausi95.citygame.adapter.`in`.web.controller.leaderboard

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.leaderboard.FoundMisterX
import net.lausi95.citygame.application.domain.model.leaderboard.Leaderboard
import net.lausi95.citygame.application.domain.model.leaderboard.LeaderboardEntry
import java.time.OffsetDateTime

@Schema(description = "The team ranking by MISTERX agents found; the teams array is in rank order, best first")
data class LeaderboardResource(

    @Schema(description = "The game's scheduled time window")
    @JsonProperty("game")
    val game: GameWindowDto,

    @Schema(description = "Teams in rank order (best first). Teams with no counted finds cluster at the end with no meaningful order among them.")
    @JsonProperty("teams")
    val teams: List<EntryDto>,
) {
    constructor(leaderboard: Leaderboard) : this(
        game = GameWindowDto(leaderboard.game.startTime, leaderboard.game.endTime),
        teams = leaderboard.entries.map { EntryDto(it) },
    )

    @Schema(description = "The game's scheduled start and end")
    data class GameWindowDto(

        @Schema(description = "When the game starts")
        @JsonProperty("startTime")
        val startTime: OffsetDateTime,

        @Schema(description = "When the game ends")
        @JsonProperty("endTime")
        val endTime: OffsetDateTime,
    )

    @Schema(description = "One team's standing")
    data class EntryDto(

        @Schema(description = "Unique identifier of the team")
        @JsonProperty("teamId")
        val teamId: String,

        @Schema(description = "Team display name")
        @JsonProperty("teamName")
        val teamName: String,

        @Schema(description = "Number of counted (active MISTERX) agents the team has found")
        @JsonProperty("foundCount")
        val foundCount: Int,

        @Schema(description = "The found MISTERX agents, earliest first")
        @JsonProperty("agents")
        val agents: List<FoundAgentDto>,
    ) {
        constructor(entry: LeaderboardEntry) : this(
            teamId = entry.team.id.value,
            teamName = entry.team.name,
            foundCount = entry.foundCount,
            agents = entry.foundAgents.map { FoundAgentDto(it) },
        )
    }

    @Schema(description = "A found MISTERX agent: its in-game alias and when it was found")
    data class FoundAgentDto(

        @Schema(description = "In-game alias")
        @JsonProperty("alias")
        val alias: String,

        @Schema(description = "When the team found this agent")
        @JsonProperty("foundAt")
        val foundAt: OffsetDateTime,
    ) {
        constructor(found: FoundMisterX) : this(found.alias, found.foundAt)
    }
}
