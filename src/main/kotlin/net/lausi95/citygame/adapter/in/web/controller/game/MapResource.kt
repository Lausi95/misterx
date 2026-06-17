package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.common.GeoLocation

data class MapResource(

    @JsonProperty("cornerA")
    val cornerA: GeoLocationDto,

    @JsonProperty("cornerB")
    val cornerB: GeoLocationDto,

    @JsonProperty("grid")
    val grid: GridDto,

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

    data class GeoLocationDto(

        @JsonProperty("latitude")
        val latitude: Double,

        @JsonProperty("longitude")
        val longitude: Double,
    ) {
        constructor(geoLocation: GeoLocation) : this(geoLocation.latitude, geoLocation.longitude)
    }

    data class GridDto(

        @JsonProperty("longitude")
        val rows: Int,

        @JsonProperty("columns")
        val columns: Int,
    ) {
        constructor(grid: Grid) : this(grid.rows, grid.columns)
    }
}
