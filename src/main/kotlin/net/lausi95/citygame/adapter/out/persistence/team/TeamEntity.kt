package net.lausi95.citygame.adapter.out.persistence.team

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

@Entity
@Table(name = "team")
internal class TeamEntity {

    @Id
    @Column(name = "id")
    var id: String? = null

    @Column(name = "tenant")
    var tenant: String? = null

    @Column(name = "game_id")
    var gameId: String? = null

    @Column(name = "name")
    var name: String? = null

    constructor(team: Team, tenant: Tenant) {
        this.id = team.id.value
        this.tenant = tenant.value
        this.gameId = team.gameId.value
        this.name = team.name
    }

    fun toTeam() = Team(
        TeamId(id!!),
        GameId(gameId!!),
        name!!,
    )
}
