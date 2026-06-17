package net.lausi95.citygame.adapter.`in`.web.controller.location

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request body to update an agent's location")
internal data class UpdateLocationRequest(

    @Schema(description = "Latitude in decimal degrees", required = true)
    @JsonProperty("latitude")
    val latitude: Double?,

    @Schema(description = "Longitude in decimal degrees", required = true)
    @JsonProperty("longitude")
    val longitude: Double?,
)
