package net.lausi95.citygame.application.port.out.finding

import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface GetTeamFindingsPort {

    fun getFindingsByTeam(teamId: TeamId, tenant: Tenant): List<AgentFinding>
}
