package net.lausi95.citygame.adapter.`in`.web.controller.board

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.board.Board
import net.lausi95.citygame.application.domain.model.board.MisterXOnBoard
import net.lausi95.citygame.application.domain.model.game.Cell
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.common.GeoLocation
import java.time.OffsetDateTime

@Schema(description = "The live playfield as a team sees it: the map overlaid with visible agent positions")
data class BoardResource(

    @Schema(description = "The game's scheduled time window")
    @JsonProperty("game")
    val game: GameWindowDto,

    @Schema(description = "The map bounded by two geographic corners with its grid overlay")
    @JsonProperty("map")
    val map: MapDto,

    @Schema(description = "UTILITY agents, shown at their exact last-known location")
    @JsonProperty("utilityAgents")
    val utilityAgents: List<UtilityAgentDto>,

    @Schema(description = "MISTERX agents, shown only as the grid cell containing their last-known location")
    @JsonProperty("misterxAgents")
    val misterxAgents: List<MisterxAgentDto>,
) {
    constructor(board: Board) : this(
        game = GameWindowDto(board.game.startTime, board.game.endTime),
        map = MapDto(board.game.map),
        utilityAgents = board.utilityAgents.map { UtilityAgentDto(it) },
        misterxAgents = board.misterxAgents.map { MisterxAgentDto(it) },
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

    @Schema(description = "The map bounded by two geographic corners")
    data class MapDto(

        @Schema(description = "South-west corner of the map area")
        @JsonProperty("cornerA")
        val cornerA: GeoLocationDto,

        @Schema(description = "North-east corner of the map area")
        @JsonProperty("cornerB")
        val cornerB: GeoLocationDto,

        @Schema(description = "Grid overlay dividing the map into cells")
        @JsonProperty("grid")
        val grid: GridDto,
    ) {
        constructor(map: Map) : this(
            cornerA = GeoLocationDto(map.cornerA),
            cornerB = GeoLocationDto(map.cornerB),
            grid = GridDto(map.grid),
        )
    }

    @Schema(description = "A UTILITY agent at its exact last-known location")
    data class UtilityAgentDto(

        @Schema(description = "Unique identifier of the agent")
        @JsonProperty("id")
        val id: String,

        @Schema(description = "In-game alias")
        @JsonProperty("alias")
        val alias: String,

        @Schema(description = "Exact last-known location")
        @JsonProperty("geoLocation")
        val geoLocation: GeoLocationDto,

        @Schema(description = "When the location was last reported")
        @JsonProperty("lastSeenAt")
        val lastSeenAt: OffsetDateTime,
    ) {
        constructor(agent: Agent) : this(
            id = agent.id.value,
            alias = agent.alias,
            geoLocation = GeoLocationDto(agent.location!!.geoLocation),
            lastSeenAt = agent.location!!.timestamp,
        )
    }

    @Schema(description = "A MISTERX agent obfuscated to the grid cell containing its last-known location")
    data class MisterxAgentDto(

        @Schema(description = "Unique identifier of the agent")
        @JsonProperty("id")
        val id: String,

        @Schema(description = "In-game alias")
        @JsonProperty("alias")
        val alias: String,

        @Schema(description = "Grid cell containing the agent's last-known location")
        @JsonProperty("cell")
        val cell: CellDto,

        @Schema(description = "When the location was last reported")
        @JsonProperty("lastSeenAt")
        val lastSeenAt: OffsetDateTime,
    ) {
        constructor(misterX: MisterXOnBoard) : this(
            id = misterX.agent.id.value,
            alias = misterX.agent.alias,
            cell = CellDto(misterX.cell),
            lastSeenAt = misterX.agent.location!!.timestamp,
        )
    }

    @Schema(description = "Geographic coordinate")
    data class GeoLocationDto(

        @Schema(description = "Latitude in decimal degrees")
        @JsonProperty("latitude")
        val latitude: Double,

        @Schema(description = "Longitude in decimal degrees")
        @JsonProperty("longitude")
        val longitude: Double,
    ) {
        constructor(geoLocation: GeoLocation) : this(geoLocation.latitude, geoLocation.longitude)
    }

    @Schema(description = "Grid dimensions for the map")
    data class GridDto(

        @Schema(description = "Number of rows in the grid")
        @JsonProperty("rows")
        val rows: Int,

        @Schema(description = "Number of columns in the grid")
        @JsonProperty("columns")
        val columns: Int,
    ) {
        constructor(grid: Grid) : this(grid.rows, grid.columns)
    }

    @Schema(description = "Zero-based grid cell index; origin (0,0) is the south-west corner")
    data class CellDto(

        @Schema(description = "Row index, increasing northward from 0")
        @JsonProperty("row")
        val row: Int,

        @Schema(description = "Column index, increasing eastward from 0")
        @JsonProperty("column")
        val column: Int,
    ) {
        constructor(cell: Cell) : this(cell.row, cell.column)
    }
}
