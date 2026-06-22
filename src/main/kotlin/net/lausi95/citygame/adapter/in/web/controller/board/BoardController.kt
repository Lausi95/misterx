package net.lausi95.citygame.adapter.`in`.web.controller.board

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.`in`.board.GetBoardUseCase
import net.lausi95.citygame.common.Tenant
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Board")
@RestController
internal class BoardController(
    private val getBoardUseCase: GetBoardUseCase,
) {

    @Operation(summary = "Returns the live playfield as a team sees it")
    @ApiResponse(
        responseCode = "200",
        description = "The current board for the given game (and team, if X-TeamId is supplied)",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = BoardResource::class))],
    )
    @ApiResponse(
        responseCode = "400",
        description = "The required X-GameId header is missing",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Game not found, or the given team is not part of the game",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping("/board")
    fun getBoard(
        @Parameter(description = "Identifier of the game", required = true)
        @RequestHeader(name = "X-GameId") gameId: String,
        @Parameter(description = "Identifier of the viewing team; restricts MISTERX agents to the ones it has not found yet")
        @RequestHeader(name = "X-TeamId", required = false) teamId: String?,
        tenant: Tenant,
    ): BoardResource {
        val query = GetBoardUseCase.Query(
            gameId = GameId(gameId),
            teamId = teamId?.let { TeamId(it) },
        )
        return BoardResource(getBoardUseCase.getBoard(query, tenant))
    }
}
