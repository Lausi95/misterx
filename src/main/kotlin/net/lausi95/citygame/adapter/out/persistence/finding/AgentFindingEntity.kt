package net.lausi95.citygame.adapter.out.persistence.finding

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.finding.FindingId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import java.time.OffsetDateTime

@Entity
@Table(name = "agent_finding")
internal class AgentFindingEntity {

    @Id
    @Column(name = "id")
    var id: String? = null

    @Column(name = "tenant")
    var tenant: String? = null

    @Column(name = "game_id")
    var gameId: String? = null

    @Column(name = "team_id")
    var teamId: String? = null

    @Column(name = "agent_id")
    var agentId: String? = null

    @Column(name = "found_at")
    var foundAt: OffsetDateTime? = null

    @Column(name = "reported_latitude")
    var reportedLatitude: Double? = null

    @Column(name = "reported_longitude")
    var reportedLongitude: Double? = null

    @Column(name = "agent_latitude")
    var agentLatitude: Double? = null

    @Column(name = "agent_longitude")
    var agentLongitude: Double? = null

    constructor(finding: AgentFinding, tenant: Tenant) {
        this.id = finding.id.value
        this.tenant = tenant.value
        this.gameId = finding.gameId.value
        this.teamId = finding.teamId.value
        this.agentId = finding.agentId.value
        this.foundAt = finding.foundAt
        this.reportedLatitude = finding.reportedLocation?.latitude
        this.reportedLongitude = finding.reportedLocation?.longitude
        this.agentLatitude = finding.agentLocation?.latitude
        this.agentLongitude = finding.agentLocation?.longitude
    }

    fun toAgentFinding() = AgentFinding(
        FindingId(id!!),
        GameId(gameId!!),
        TeamId(teamId!!),
        AgentId(agentId!!),
        foundAt!!,
        toGeoLocation(reportedLatitude, reportedLongitude),
        toGeoLocation(agentLatitude, agentLongitude),
    )

    private fun toGeoLocation(latitude: Double?, longitude: Double?): GeoLocation? {
        return if (latitude != null && longitude != null) GeoLocation(latitude, longitude) else null
    }
}
