package net.lausi95.citygame.application.domain.model.agentlocation

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.common.GeoLocation
import java.time.ZonedDateTime
import java.util.*

@JvmInline
value class AgentLocationId(val value: String = UUID.randomUUID().toString()) {
    override fun toString(): String = value
}

class AgentLocation(
    val id: AgentLocationId,
    private var _agentId: AgentId,
    private var _timestamp: ZonedDateTime,
    private var _geoLocation: GeoLocation,
) {

    val agentId: AgentId
        get() = _agentId

    val timestamp: ZonedDateTime
        get() = _timestamp

    val geoLocation: GeoLocation
        get() = _geoLocation
}
