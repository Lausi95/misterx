package net.lausi95.citygame.adapter.out.persistence.agentlocation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocationId
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import java.time.OffsetDateTime

@Entity
@Table(name = "agent_location")
internal class AgentLocationEntity {

    @Id
    @Column(name = "id")
    var id: String? = null

    @Column(name = "tenant")
    var tenant: String? = null

    @Column(name = "agent_id")
    var agentId: String? = null

    @Column(name = "timestamp")
    var timestamp: OffsetDateTime? = null

    @Column(name = "latitude")
    var latitude: Double? = null

    @Column(name = "longitude")
    var longitude: Double? = null

    constructor(agentLocation: AgentLocation, tenant: Tenant) {
        this.id = agentLocation.id.value
        this.tenant = tenant.value
        this.agentId = agentLocation.agentId.value
        this.timestamp = agentLocation.timestamp
        this.latitude = agentLocation.geoLocation.latitude
        this.longitude = agentLocation.geoLocation.longitude
    }

    fun toAgentLocation(): AgentLocation = AgentLocation(
        AgentLocationId(id!!),
        AgentId(agentId!!),
        timestamp!!,
        GeoLocation(latitude!!, longitude!!),
    )
}
