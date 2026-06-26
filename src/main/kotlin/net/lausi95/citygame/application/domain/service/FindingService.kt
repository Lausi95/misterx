package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.agentNotFound
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.finding.FindingId
import net.lausi95.citygame.application.domain.model.finding.FindingTeam
import net.lausi95.citygame.application.domain.model.finding.FoundAgent
import net.lausi95.citygame.application.domain.model.finding.agentAlreadyFound
import net.lausi95.citygame.application.domain.model.finding.agentNotFindable
import net.lausi95.citygame.application.domain.model.game.gameNotActive
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.teamMemberNotFound
import net.lausi95.citygame.application.domain.model.team.teamNotFound
import net.lausi95.citygame.application.port.`in`.finding.FindingUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.TeamMemberRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

private val log = KotlinLogging.logger { }

@Service
class FindingService(
    private val gameRepository: GameRepository,
    private val agentRepository: AgentRepository,
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val agentLocationRepository: AgentLocationRepository,
    private val findingRepository: FindingRepository,
) : FindingUseCase {

    @Transactional
    override fun findAgent(command: FindingUseCase.FindAgentCommand, tenant: Tenant): FindingId {
        log.info { "Team ${command.teamId} attempting to find agent ${command.agentId}..." }

        val game = gameRepository.get(command.gameId, tenant)
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        if (now.isBefore(game.startTime) || now.isAfter(game.endTime)) {
            gameNotActive(command.gameId)
        }

        val agent = agentRepository.getOrNull(command.agentId, tenant)
            ?.takeIf { it.gameId == command.gameId }
            ?: agentNotFound(command.agentId)

        val team = teamRepository.getOrNull(command.teamId, tenant)
            ?.takeIf { it.gameId == command.gameId }
            ?: teamNotFound(command.teamId)

        val member = teamMemberRepository.getOrNull(command.memberId, tenant)
        if (member == null || member.teamId != team.id || member.gameId != command.gameId) {
            teamMemberNotFound(command.memberId)
        }

        if (!agent.active || agent.type != Agent.Type.MISTERX) {
            agentNotFindable(command.agentId)
        }

        if (findingRepository.existsByTeamAndAgent(command.teamId, command.agentId, tenant)) {
            agentAlreadyFound(command.teamId, command.agentId)
        }

        val agentLocation = agentLocationRepository.latest(command.agentId)?.geoLocation

        val finding = AgentFinding(
            FindingId(),
            command.gameId,
            command.teamId,
            command.agentId,
            OffsetDateTime.now(ZoneOffset.UTC),
            command.reportedLocation,
            agentLocation,
        )

        findingRepository.save(finding, tenant)

        log.info { "Agent ${command.agentId} found by team ${command.teamId} (finding ${finding.id})." }

        return finding.id
    }

    override fun getFindingTeams(agentId: AgentId, tenant: Tenant): List<FindingTeam> {
        return findingRepository.byAgent(agentId, tenant).mapNotNull { finding ->
            teamRepository.getOrNull(finding.teamId, tenant)?.let { team ->
                FindingTeam(team.id, team.name, finding.foundAt)
            }
        }
    }

    override fun getFoundAgents(teamId: TeamId, tenant: Tenant): List<FoundAgent> {
        return findingRepository.byTeam(teamId, tenant).mapNotNull { finding ->
            agentRepository.getOrNull(finding.agentId, tenant)?.let { agent ->
                FoundAgent(agent.id, agent.alias, finding.foundAt)
            }
        }
    }

    override fun getFoundAgentsByTeams(teamIds: Collection<TeamId>, tenant: Tenant): Map<TeamId, List<FoundAgent>> {
        val findings = findingRepository.byTeams(teamIds, tenant)
        val agentsById = agentRepository.byIds(findings.map { it.agentId }.toSet(), tenant)
            .associateBy { it.id }
        return findings
            .mapNotNull { finding ->
                agentsById[finding.agentId]?.let { agent ->
                    finding.teamId to FoundAgent(agent.id, agent.alias, finding.foundAt)
                }
            }
            .groupBy({ it.first }, { it.second })
    }
}
