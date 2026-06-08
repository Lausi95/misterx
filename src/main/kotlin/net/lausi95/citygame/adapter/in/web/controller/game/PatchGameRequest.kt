package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

data class PatchGameRequest(

    @field:Parameter(name = "title", description = "Title of the game", required = false)
    @field:JsonProperty("title")
    var title: String?,

    @field:Parameter(name = "startTime", description = "Start date of the game", required = false)
    @field:JsonProperty("startTime")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var startTime: OffsetDateTime?,

    @field:Parameter(name = "endTime", description = "End date of the game", required = false)
    @field:JsonProperty("endTime")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var endTime: OffsetDateTime?,

    var map: MapDto?,

    @field:Parameter(name = "point1", description = "Point of the playfield", required = false)
    @field:JsonProperty("point1")
    var point1: GeoLocationDto?,

    @field:Parameter(name = "point2", description = "Point of the playfield", required = false)
    @field:JsonProperty("point2")
    var point2: GeoLocationDto?,

    @field:Parameter(name = "grid", description = "Grid of the playfield", required = false)
    @field:JsonProperty("grid")
    var grid: GridDto?,
) {
    data class MapDto(

        @field:Parameter(name = "cornerA", description = "Corner A of the map", required = true)
        @field:JsonProperty("cornerA")
        var cornerA: GeoLocationDto?,

        @field:Parameter(name = "cornerB", description = "Corner B of the map", required = true)
        @field:JsonProperty("cornerB")
        var cornerB: GeoLocationDto?,

        @field:Parameter(name = "grid", description = "Grid of the Map", required = true)
        @field:JsonProperty("grid")
        var grid: GridDto?,
    )

    data class GeoLocationDto(

        @field:Parameter(name = "latitude", description = "Latitude of the GeoLocation", required = true)
        @field:JsonProperty("latitude")
        @field:NotNull
        var latitude: Double?,

        @field:Parameter(name = "longitude", description = "Longitude of the GeoLocation", required = true)
        @field:JsonProperty("longitude")
        @field:NotNull
        var longitude: Double?,
    )

    data class GridDto(

        @field:Parameter(name = "rows", description = "Amount of rows in the grid", required = true)
        @field:JsonProperty("rows")
        @field:NotNull
        var rows: Int?,

        @field:Parameter(name = "columns", description = "Amount of columns in the grid", required = true)
        @field:JsonProperty("columns")
        @field:NotNull
        var columns: Int?,
    )
}
