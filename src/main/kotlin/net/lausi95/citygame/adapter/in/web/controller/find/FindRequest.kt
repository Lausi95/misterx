package net.lausi95.citygame.adapter.`in`.web.controller.find

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Optional body reporting the finding member's own location at the moment of the find")
internal data class FindRequest(

    @Schema(description = "Latitude in decimal degrees where the team reports finding the agent", required = false)
    @JsonProperty("latitude")
    val latitude: Double?,

    @Schema(description = "Longitude in decimal degrees where the team reports finding the agent", required = false)
    @JsonProperty("longitude")
    val longitude: Double?,
)
