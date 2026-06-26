package net.lausi95.citygame.application.port.`in`.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.FindingId
import net.lausi95.citygame.application.domain.model.finding.FindingTeam
import net.lausi95.citygame.application.domain.model.finding.FoundAgent
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant

interface FindingUseCase {

    data class FindAgentCommand(
        val gameId: GameId,
        val teamId: TeamId,
        val memberId: TeamMemberId,
        val agentId: AgentId,
        val reportedLocation: GeoLocation?,
    )

    fun findAgent(command: FindAgentCommand, tenant: Tenant): FindingId

    fun getFindingTeams(agentId: AgentId, tenant: Tenant): List<FindingTeam>

    fun getFoundAgents(teamId: TeamId, tenant: Tenant): List<FoundAgent>

    fun getFoundAgentsByTeams(teamIds: Collection<TeamId>, tenant: Tenant): Map<TeamId, List<FoundAgent>>
}
