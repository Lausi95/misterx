package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.board.Board
import net.lausi95.citygame.application.domain.model.board.MisterXOnBoard
import net.lausi95.citygame.application.domain.model.team.teamNotFound
import net.lausi95.citygame.application.port.`in`.board.GetBoardUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.GetAgentLocationPort
import net.lausi95.citygame.application.port.out.finding.GetTeamFindingsPort
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class GetBoardService(
    private val gameRepository: GameRepository,
    private val getTeamPort: GetTeamPort,
    private val agentRepository: AgentRepository,
    private val getAgentLocationPort: GetAgentLocationPort,
    private val getTeamFindingsPort: GetTeamFindingsPort,
) : GetBoardUseCase {

    @Transactional(readOnly = true)
    override fun getBoard(query: GetBoardUseCase.Query, tenant: Tenant): Board {
        log.info { "Building board for game ${query.gameId} (team ${query.teamId})..." }

        val game = gameRepository.get(query.gameId, tenant)

        // When a team is viewing, it must belong to the game; collect the agents it has found so
        // they can be hidden from its board.
        val foundAgentIds = query.teamId?.let { teamId ->
            getTeamPort.getTeamOrNull(teamId, tenant)
                ?.takeIf { it.gameId == query.gameId }
                ?: teamNotFound(teamId)
            getTeamFindingsPort.getFindingsByTeam(teamId, tenant)
                .map { it.agentId }
                .toSet()
        } ?: emptySet()

        val agents = agentRepository.forGame(query.gameId, tenant)
            .onEach { agent ->
                getAgentLocationPort.getAgentLocation(agent.id)?.also { agent.setLocation(it) }
            }
            .filter { it.active && it.location != null }

        val utilityAgents = agents.filter { it.type == Agent.Type.UTILITY }

        val misterxAgents = agents
            .filter { it.type == Agent.Type.MISTERX }
            .filterNot { it.id in foundAgentIds }
            .mapNotNull { agent ->
                game.map.cellOf(agent.location!!.geoLocation)?.let { cell -> MisterXOnBoard(agent, cell) }
            }

        log.info { "Board built: ${utilityAgents.size} utility, ${misterxAgents.size} misterx visible." }
        return Board(game, utilityAgents, misterxAgents)
    }
}
