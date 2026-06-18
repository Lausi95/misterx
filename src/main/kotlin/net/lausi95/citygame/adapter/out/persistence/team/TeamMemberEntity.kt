package net.lausi95.citygame.adapter.out.persistence.team

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.common.Tenant
import java.time.Instant

@Entity
@Table(name = "team_member")
internal class TeamMemberEntity {

    @Id
    @Column(name = "id")
    var id: String? = null

    @Column(name = "tenant")
    var tenant: String? = null

    @Column(name = "game_id")
    var gameId: String? = null

    @Column(name = "team_id")
    var teamId: String? = null

    @Column(name = "registered_at")
    var registeredAt: Instant? = null

    constructor(member: TeamMember, tenant: Tenant) {
        this.id = member.id.value
        this.tenant = tenant.value
        this.gameId = member.gameId.value
        this.teamId = member.teamId.value
        this.registeredAt = member.registeredAt
    }

    fun toTeamMember() = TeamMember(
        TeamMemberId(id!!),
        TeamId(teamId!!),
        GameId(gameId!!),
        registeredAt!!,
    )
}
