package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

data class CreateGameRequest(

    @Parameter(name = "title", description = "Title of the game", required = true)
    @JsonProperty("title")
    @NotEmpty(message = "'title' cannot be null or empty")
    var title: String?,

    @Parameter(name = "startTime", description = "Start date of the game", required = true)
    @JsonProperty("startTime")
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var startTime: OffsetDateTime?,

    @Parameter(name = "endTime", description = "End date of the game", required = true)
    @JsonProperty("endTime")
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var endTime: OffsetDateTime?,

    @Parameter(name = "map", description = "Map of the game", required = true)
    @JsonProperty("map")
    @NotNull
    var map: MapDto?
) {
    data class MapDto(

        @Parameter(name = "cornerA", description = "Corner A of the map", required = true)
        @JsonProperty("cornerA")
        @NotNull
        var cornerA: GeoLocationDto?,

        @Parameter(name = "cornerB", description = "Corner B of the map", required = true)
        @JsonProperty("cornerB")
        @NotNull
        var cornerB: GeoLocationDto?,

        @Parameter(name = "grid", description = "Grid of the Map", required = true)
        @JsonProperty("grid")
        @NotNull
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
