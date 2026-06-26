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
import net.lausi95.citygame.application.port.`in`.game.GameUseCase
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
    private val gameUseCase: GameUseCase,
) {

    @PostMapping
    @Operation(summary = "Creates a new game")
    @ApiResponse(
        responseCode = "201",
        description = "Game was created successfully",
        headers = [
            Header(name = "Location", description = "URI of the new game"),
            Header(name = "X-GameId", description = "Identifier of the created game"),
        ],
        content = [],
    )
    @ApiResponse(
        responseCode = "400",
        description = "Input validation errors",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    fun postGame(
        @RequestBody @Valid request: CreateGameRequest,
        tenant: Tenant
    ): ResponseEntity<Unit> {
        val command = GameUseCase.CreateGameCommand(
            title = GameTitle(requireNotNull(request.title)),
            startTime = requireNotNull(request.startTime),
            endTime = requireNotNull(request.endTime),
            map = GameUseCase.CreateGameCommand.MapDto(
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

        val gameId = gameUseCase.createGame(command, tenant)

        val uri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/games/{gameId}")
            .build(gameId.value)

        return ResponseEntity.created(uri).headers {
            it.set("X-GameId", gameId.value)
        }.build()
    }

    @Operation(summary = "Returns a specific game")
    @ApiResponse(
        responseCode = "200",
        description = "Game with the given ID",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = GameResource::class))],
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
    @GetMapping("/{gameId}")
    fun getGame(
        @PathVariable gameId: String,
        tenant: Tenant
    ): GameResource {
        val game = gameUseCase.getGame(GameId(gameId), tenant)
        return GameResource(game)
    }

    @Operation(summary = "Returns a paginated collection of games")
    @ApiResponse(
        responseCode = "200",
        description = "Collection of games",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = GameCollection::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping
    fun getGames(
        @PageableDefault pageable: Pageable,
        tenant: Tenant,
    ): GameCollection {
        val games = gameUseCase.getGames(pageable, tenant)
        return GameCollection(games)
    }

    @Operation(summary = "Returns the map for a specific game")
    @ApiResponse(
        responseCode = "200",
        description = "Map for the given game",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = MapResource::class))],
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
    @GetMapping("/{gameId}/map")
    fun getMap(
        @PathVariable gameId: String,
        tenant: Tenant,
    ): MapResource {
        return MapResource(GameId(gameId), gameUseCase.getMap(GameId(gameId), tenant))
    }

    @Operation(summary = "Updates the editable fields of a game")
    @ApiResponse(
        responseCode = "202",
        description = "Game was updated successfully",
        content = [],
    )
    @ApiResponse(
        responseCode = "400",
        description = "At least one field contained a validation error",
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
    @PatchMapping("/{gameId}")
    fun updateGame(
        @PathVariable gameId: String,
        @RequestBody @Valid request: PatchGameRequest,
        tenant: Tenant,
    ): ResponseEntity<Unit> {
        val command = GameUseCase.UpdateGameCommand(
            gameId = GameId(gameId),
            title = request.title?.let { GameTitle(it) },
            startTime = request.startTime,
            endTime = request.endTime,
            map = GameUseCase.UpdateGameMapDto(
                cornerA = request.map?.cornerA?.let {
                    GeoLocation(
                        latitude = requireNotNull(it.latitude),
                        longitude = requireNotNull(it.longitude),
                    )
                },
                cornerB = request.map?.cornerB?.let {
                    GeoLocation(
                        latitude = requireNotNull(it.latitude),
                        longitude = requireNotNull(it.longitude),
                    )
                },
                grid = request.map?.grid?.let {
                    Grid(
                        rows = requireNotNull(it.rows),
                        columns = requireNotNull(it.columns),
                    )
                },
            )
        )
        gameUseCase.updateGame(command, tenant)
        return ResponseEntity.accepted().build()
    }
}
