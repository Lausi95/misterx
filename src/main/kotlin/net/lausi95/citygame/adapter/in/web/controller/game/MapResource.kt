package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.common.GeoLocation

@Schema(description = "The game map bounded by two geographic corners")
data class MapResource(

    @Schema(description = "South-west corner of the map area")
    @JsonProperty("cornerA")
    val cornerA: GeoLocationDto,

    @Schema(description = "North-east corner of the map area")
    @JsonProperty("cornerB")
    val cornerB: GeoLocationDto,

    @Schema(description = "Grid overlay dividing the map into cells")
    @JsonProperty("grid")
    val grid: GridDto,

    @Schema(description = "Navigation links")
    @JsonProperty("links")
    val links: kotlin.collections.Map<String, String>
) {
    constructor(gameId: GameId, map: Map) : this(
        cornerA = GeoLocationDto(map.cornerA),
        cornerB = GeoLocationDto(map.cornerB),
        grid = GridDto(map.grid),
        links = mapOf(
            "self" to "/games/${gameId.value}/map"
        )
    )

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
        @JsonProperty("longitude")
        val rows: Int,

        @Schema(description = "Number of columns in the grid")
        @JsonProperty("columns")
        val columns: Int,
    ) {
        constructor(grid: Grid) : this(grid.rows, grid.columns)
    }
}
