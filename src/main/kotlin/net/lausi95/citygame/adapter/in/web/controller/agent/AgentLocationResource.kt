package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

@Schema(description = "Last known location of an agent")
data class AgentLocationResource(

    @Schema(description = "When the location was recorded")
    @JsonProperty("timestamp")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val timestamp: OffsetDateTime,

    @Schema(description = "Latitude in decimal degrees")
    @JsonProperty("latitude")
    val latitude: Double,

    @Schema(description = "Longitude in decimal degrees")
    @JsonProperty("longitude")
    val longitude: Double,
) {

    constructor(location: AgentLocation) : this(
        timestamp = location.timestamp,
        latitude = location.geoLocation.latitude,
        longitude = location.geoLocation.longitude,
    )
}
