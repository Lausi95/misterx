package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.common.GeoLocation

data class MapResource(

    @field:JsonProperty("cornerA")
    val cornerA: GeoLocationDto,

    @field:JsonProperty("cornerB")
    val cornerB: GeoLocationDto,

    @field:JsonProperty("grid")
    val grid: GridDto,

    @field:JsonProperty("links")
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
        val latitude: Double,
        val longitude: Double,
    ) {
        constructor(geoLocation: GeoLocation) : this(geoLocation.latitude, geoLocation.longitude)
    }

    data class GridDto(
        val rows: Int,
        val column: Int,
    ) {
        constructor(grid: Grid) : this(grid.rows, grid.columns)
    }
}
