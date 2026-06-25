package net.lausi95.citygame.application.port.out.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

/**
 * The persistence seam for the Finding aggregate. One repository per aggregate (ADR 0017),
 * replacing the former single-method ports.
 */
interface FindingRepository {

    fun save(agentFinding: AgentFinding, tenant: Tenant)

    fun existsByTeamAndAgent(teamId: TeamId, agentId: AgentId, tenant: Tenant): Boolean

    fun byTeam(teamId: TeamId, tenant: Tenant): List<AgentFinding>

    fun byTeams(teamIds: Collection<TeamId>, tenant: Tenant): List<AgentFinding>

    fun byAgent(agentId: AgentId, tenant: Tenant): List<AgentFinding>

    fun deleteByTeam(teamId: TeamId, tenant: Tenant)

    fun deleteByAgent(agentId: AgentId, tenant: Tenant)
}
