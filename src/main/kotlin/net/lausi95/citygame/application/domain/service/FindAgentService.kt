package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.agentNotFound
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.finding.FindingId
import net.lausi95.citygame.application.domain.model.finding.agentAlreadyFound
import net.lausi95.citygame.application.domain.model.finding.agentNotFindable
import net.lausi95.citygame.application.domain.model.game.gameNotActive
import net.lausi95.citygame.application.domain.model.team.teamMemberNotFound
import net.lausi95.citygame.application.domain.model.team.teamNotFound
import net.lausi95.citygame.application.port.`in`.finding.FindAgentUseCase
import net.lausi95.citygame.application.port.out.agent.GetAgentPort
import net.lausi95.citygame.application.port.out.agentlocation.GetAgentLocationPort
import net.lausi95.citygame.application.port.out.finding.CheckAgentFoundByTeamPort
import net.lausi95.citygame.application.port.out.finding.SaveAgentFindingPort
import net.lausi95.citygame.application.port.out.game.GetGamePort
import net.lausi95.citygame.application.port.out.team.GetTeamMemberPort
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZonedDateTime

private val log = KotlinLogging.logger { }

@Service
class FindAgentService(
    private val getGamePort: GetGamePort,
    private val getAgentPort: GetAgentPort,
    private val getTeamPort: GetTeamPort,
    private val getTeamMemberPort: GetTeamMemberPort,
    private val getAgentLocationPort: GetAgentLocationPort,
    private val checkAgentFoundByTeamPort: CheckAgentFoundByTeamPort,
    private val saveAgentFindingPort: SaveAgentFindingPort,
) : FindAgentUseCase {

    @Transactional
    override fun findAgent(command: FindAgentUseCase.Command, tenant: Tenant): FindingId {
        log.info { "Team ${command.teamId} attempting to find agent ${command.agentId}..." }

        // 1. Game must exist and be currently active.
        val game = getGamePort.getGame(command.gameId, tenant)
        val now = OffsetDateTime.now()
        if (now.isBefore(game.startTime) || now.isAfter(game.endTime)) {
            gameNotActive(command.gameId)
        }

        // 2. Agent must exist and belong to the game.
        val agent = getAgentPort.getAgentOrNull(command.agentId, tenant)
            ?.takeIf { it.gameId == command.gameId }
            ?: agentNotFound(command.agentId)

        // 3. Team must exist and belong to the game.
        val team = getTeamPort.getTeamOrNull(command.teamId, tenant)
            ?.takeIf { it.gameId == command.gameId }
            ?: teamNotFound(command.teamId)

        // 4. Member must exist and belong to both the team and the game.
        val member = getTeamMemberPort.getTeamMemberOrNull(command.memberId, tenant)
        if (member == null || member.teamId != team.id || member.gameId != command.gameId) {
            teamMemberNotFound(command.memberId)
        }

        // 5. Only active MISTERX agents are findable.
        if (!agent.active || agent.type != Agent.Type.MISTERX) {
            agentNotFindable(command.agentId)
        }

        // 6. A team can find a given agent only once.
        if (checkAgentFoundByTeamPort.teamHasFoundAgent(command.teamId, command.agentId, tenant)) {
            agentAlreadyFound(command.teamId, command.agentId)
        }

        val agentLocation = getAgentLocationPort.getAgentLocation(command.agentId)?.geoLocation

        val finding = AgentFinding(
            FindingId(),
            command.gameId,
            command.teamId,
            command.agentId,
            ZonedDateTime.now(),
            command.reportedLocation,
            agentLocation,
        )

        saveAgentFindingPort.saveAgentFinding(finding, tenant)

        log.info { "Agent ${command.agentId} found by team ${command.teamId} (finding ${finding.id})." }

        return finding.id
    }
}
