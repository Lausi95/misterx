package net.lausi95.citygame.adapter.`in`.web.controller.game

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.port.`in`.game.CreateGameUseCase
import net.lausi95.citygame.application.port.`in`.game.GetGameUseCase
import net.lausi95.citygame.application.port.`in`.game.GetGamesUseCase
import net.lausi95.citygame.application.port.`in`.game.UpdateGameUseCase
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
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
        val command = CreateGameUseCase.Command(
            title = GameTitle(requireNotNull(request.title)),
            startTime = requireNotNull(request.startTime),
            endTime = requireNotNull(request.endTime),
            map = CreateGameUseCase.Command.MapDto(
                cornerA = GeoLocation(
                    latitude = requireNotNull(request.map?.cornerA?.latitude),
                    longitude = requireNotNull(request.map?.cornerA?.longitude),
                ),
                cornerB = GeoLocation(
                    latitude = requireNotNull(request.map?.cornerB?.latitude),
                    longitude = requireNotNull(request.map?.cornerB?.longitude),
                ),
                grid = Grid(
                    rows = requireNotNull(request.map?.grid?.rows),
                    columns = requireNotNull(request.map?.grid?.columns),
                )
            )
        )

        val gameId = createGameUseCase.createGame(command, tenant)

        val uri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/games/{gameId}")
            .build(gameId.value)

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
        val game = getGameUseCase.getGame(GameId(gameId), tenant)
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
        val games = getGamesUseCase.getGames(pageable, tenant)
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
        val command = UpdateGameUseCase.Command(
            gameId = GameId(gameId),
            title = request.title?.let { GameTitle(it) },
            startTime = request.startTime,
            endTime = request.endTime,
            map = UpdateGameUseCase.MapDto(
                cornerA = request.point1?.let {
                    GeoLocation(
                        latitude = requireNotNull(it.latitude),
                        longitude = requireNotNull(it.longitude),
                    )
                },
                cornerB = request.point2?.let {
                    GeoLocation(
                        latitude = requireNotNull(it.latitude),
                        longitude = requireNotNull(it.longitude),
                    )
                },
                grid = request.grid?.let {
                    Grid(
                        rows = requireNotNull(it.rows),
                        columns = requireNotNull(it.columns),
                    )
                },
            )
        )
        updateGameUseCase.updateGame(command, tenant)
        return ResponseEntity.accepted().build()
    }
}
