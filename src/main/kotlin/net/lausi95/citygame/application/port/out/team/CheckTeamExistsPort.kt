package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface CheckTeamExistsPort {

    fun teamExists(teamId: TeamId, tenant: Tenant): Boolean

    fun requireTeamExists(teamId: TeamId, tenant: Tenant) =
        require(teamExists(teamId, tenant)) { "Team does not exist." }
}
