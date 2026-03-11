package net.lausi95.citygame.infrastructure.web.controller.game

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.lausi95.citygame.application.usecase.game.creategame.CreateGameCommand
import net.lausi95.citygame.application.usecase.game.creategame.CreateGameUseCase
import net.lausi95.citygame.application.usecase.game.getgame.GetGameUseCase
import net.lausi95.citygame.application.usecase.game.getgames.GetGamesUseCase
import net.lausi95.citygame.application.usecase.game.updategame.UpdateGameCommand
import net.lausi95.citygame.application.usecase.game.updategame.UpdateGameUseCase
import net.lausi95.citygame.domain.Tenant
import net.lausi95.citygame.domain.game.GameId
import net.lausi95.citygame.domain.game.GameTitle
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Tag(name = "Games")
@RestController
@RequestMapping("/games")
class GameController(
    private val createGameUseCase: CreateGameUseCase,
    private val getGameUseCase: GetGameUseCase,
    private val getGamesUseCase: GetGamesUseCase,
    private val updateGameUseCase: UpdateGameUseCase,
) {

    @PostMapping
    @Operation(summary = "Creates a new game")
    @ApiResponse(
        responseCode = "201",
        description = "Game was created successfully",
        headers = [Header(name = "location", description = "URI of the new game")],
        content = [],
    )
    @ApiResponse(
        responseCode = "400",
        description = "Input Validation Errors",
        content = [Content(mediaType = "application/json", schema = Schema(ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error",
        content = [Content(mediaType = "application/json", schema = Schema(ProblemDetail::class))],
    )
    fun postGame(
        @RequestBody @Valid request: CreateGameRequest,
        @RequestAttribute tenant: Tenant
    ): ResponseEntity<Unit> {
        val command = CreateGameCommand(
            title = GameTitle(requireNotNull(request.title)),
            startTime = requireNotNull(request.startTime),
            endTime = requireNotNull(request.endTime)
        )

        val result = createGameUseCase(command, tenant)

        val uri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/games/{gameId}")
            .build(result.gameId.value)

        return ResponseEntity.created(uri).build()
    }

    @Operation(summary = "Returns a specific game")
    @ApiResponse(
        responseCode = "200",
        description = "Game with the given ID",
        content = [Content(mediaType = "application/json", schema = Schema(GameResource::class))],
    )
    @GetMapping("/{gameId}")
    fun getGame(
        @PathVariable gameId: String,
        @RequestAttribute tenant: Tenant
    ): GameResource {
        val game = getGameUseCase(GameId(gameId), tenant)
        return GameResource(game)
    }

    @Operation(summary = "Collection of games")
    @ApiResponse(
        responseCode = "200",
        description = "Collection of games",
        content = [Content(mediaType = "application/json", schema = Schema(GameCollection::class))],
    )
    @GetMapping
    fun getGames(
        @PageableDefault pageable: Pageable,
        @RequestAttribute tenant: Tenant,
    ): GameCollection {
        val games = getGamesUseCase(pageable, tenant)
        return GameCollection(games)
    }

    @Operation(summary = "Updates the editable fields of a game")
    @ApiResponse(
        responseCode = "202",
        description = "Game was updated successfully",
        content = [],
    )
    @ApiResponse(
        responseCode = "400",
        description = "Game was not updates. At least one of the fields contained errors.",
        content = [],
    )
    @PatchMapping("/{gameId}")
    fun updateGame(
        @PathVariable gameId: String,
        @RequestBody @Valid request: PatchGameRequest,
        @RequestAttribute tenant: Tenant,
    ): ResponseEntity<Unit> {
        val command = UpdateGameCommand(
            gameId = GameId(gameId),
            title = request.title?.let { GameTitle(it) },
            startTime = request.startTime,
            endTime = request.endTime,
        )
        updateGameUseCase(command, tenant)
        return ResponseEntity.accepted().build()
    }
}
