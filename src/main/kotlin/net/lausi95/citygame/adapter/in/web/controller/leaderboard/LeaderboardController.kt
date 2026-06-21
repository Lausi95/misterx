package net.lausi95.citygame.adapter.`in`.web.controller.leaderboard

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.leaderboard.GetLeaderboardUseCase
import net.lausi95.citygame.common.Tenant
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Leaderboard")
@RestController
internal class LeaderboardController(
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
) {

    @Operation(summary = "Returns the team ranking by the number of MISTERX agents found")
    @ApiResponse(
        responseCode = "200",
        description = "The ranked teams for the given game; array order is the ranking, best first",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = LeaderboardResource::class))],
    )
    @ApiResponse(
        responseCode = "400",
        description = "The required X-GameId header is missing",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Game not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping("/leaderboard")
    fun getLeaderboard(
        @Parameter(description = "Identifier of the game", required = true)
        @RequestHeader(name = "X-GameId") gameId: String,
        @RequestAttribute tenant: Tenant,
    ): LeaderboardResource {
        return LeaderboardResource(getLeaderboardUseCase.getLeaderboard(GameId(gameId), tenant))
    }
}
