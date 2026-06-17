package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

data class PatchGameRequest(

    @Parameter(name = "title", description = "Title of the game", required = false)
    @JsonProperty("title")
    var title: String?,

    @Parameter(name = "startTime", description = "Start date of the game", required = false)
    @JsonProperty("startTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var startTime: OffsetDateTime?,

    @Parameter(name = "endTime", description = "End date of the game", required = false)
    @JsonProperty("endTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var endTime: OffsetDateTime?,

    @Parameter(name = "map", description = "Map of the game", required = false)
    @JsonProperty("map")
    var map: MapDto?,
) {
    data class MapDto(

        @Parameter(name = "cornerA", description = "Corner A of the map", required = true)
        @JsonProperty("cornerA")
        var cornerA: GeoLocationDto?,

        @Parameter(name = "cornerB", description = "Corner B of the map", required = true)
        @JsonProperty("cornerB")
        var cornerB: GeoLocationDto?,

        @Parameter(name = "grid", description = "Grid of the Map", required = true)
        @JsonProperty("grid")
        var grid: GridDto?,
    )

    data class GeoLocationDto(

        @Parameter(name = "latitude", description = "Latitude of the GeoLocation", required = true)
        @JsonProperty("latitude")
        @NotNull
        var latitude: Double?,

        @Parameter(name = "longitude", description = "Longitude of the GeoLocation", required = true)
        @JsonProperty("longitude")
        @NotNull
        var longitude: Double?,
    )

    data class GridDto(

        @Parameter(name = "rows", description = "Amount of rows in the grid", required = true)
        @JsonProperty("rows")
        @NotNull
        var rows: Int?,

        @Parameter(name = "columns", description = "Amount of columns in the grid", required = true)
        @JsonProperty("columns")
        @NotNull
        var columns: Int?,
    )
}
