package net.lausi95.citygame.adapter.`in`.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

@Schema(description = "Request body to partially update a game")
data class PatchGameRequest(

    @Schema(description = "Title of the game")
    @JsonProperty("title")
    var title: String?,

    @Schema(description = "Start date of the game")
    @JsonProperty("startTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var startTime: OffsetDateTime?,

    @Schema(description = "End date of the game")
    @JsonProperty("endTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var endTime: OffsetDateTime?,

    @Schema(description = "Map of the game")
    @JsonProperty("map")
    var map: MapDto?,
) {
    @Schema(description = "Map boundaries and grid configuration")
    data class MapDto(

        @Schema(description = "Corner A of the map", required = true)
        @JsonProperty("cornerA")
        var cornerA: GeoLocationDto?,

        @Schema(description = "Corner B of the map", required = true)
        @JsonProperty("cornerB")
        var cornerB: GeoLocationDto?,

        @Schema(description = "Grid of the map", required = true)
        @JsonProperty("grid")
        var grid: GridDto?,
    )

    @Schema(description = "Geographic coordinate")
    data class GeoLocationDto(

        @Schema(description = "Latitude in decimal degrees", required = true)
        @JsonProperty("latitude")
        @NotNull
        var latitude: Double?,

        @Schema(description = "Longitude in decimal degrees", required = true)
        @JsonProperty("longitude")
        @NotNull
        var longitude: Double?,
    )

    @Schema(description = "Grid dimensions")
    data class GridDto(

        @Schema(description = "Number of rows in the grid", required = true)
        @JsonProperty("rows")
        @NotNull
        var rows: Int?,

        @Schema(description = "Number of columns in the grid", required = true)
        @JsonProperty("columns")
        @NotNull
        var columns: Int?,
    )
}
