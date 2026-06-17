package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import org.springframework.format.annotation.DateTimeFormat
import java.time.ZonedDateTime

data class AgentLocationResource(

    @JsonProperty("timestamp")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val timestamp: ZonedDateTime,

    @JsonProperty("latitude")
    val latitude: Double,

    @JsonProperty("longitude")
    val longitude: Double,
) {

    constructor(location: AgentLocation) : this(
        timestamp = location.timestamp,
        latitude = location.geoLocation.latitude,
        longitude = location.geoLocation.longitude,
    )
}
