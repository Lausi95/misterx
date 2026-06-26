package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.board.Board
import net.lausi95.citygame.application.domain.model.board.MisterXOnBoard
import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameSummary
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.game.MapId
import net.lausi95.citygame.application.domain.model.leaderboard.FoundMisterX
import net.lausi95.citygame.application.domain.model.leaderboard.Leaderboard
import net.lausi95.citygame.application.domain.model.leaderboard.LeaderboardEntry
import net.lausi95.citygame.application.domain.model.team.teamNotFound
import net.lausi95.citygame.application.port.`in`.game.GameUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class GameService(
    private val gameRepository: GameRepository,
    private val teamRepository: TeamRepository,
    private val agentRepository: AgentRepository,
    private val agentLocationRepository: AgentLocationRepository,
    private val findingRepository: FindingRepository,
) : GameUseCase {

    @Transactional
    override fun createGame(command: GameUseCase.CreateGameCommand, tenant: Tenant): GameId {
        log.info { "Creating new game..." }

        gameRepository.requireTitleAvailable(command.title, tenant)

        val game = Game(
            GameId(),
            command.title,
            command.startTime,
            command.endTime,
            Map(
                MapId(),
                command.map.cornerA,
                command.map.cornerB,
                command.map.grid,
            ),
        )

        gameRepository.save(game, tenant)

        log.info { "Game created." }

        return game.id
    }

    override fun getGames(pageable: Pageable, tenant: Tenant): Page<GameSummary> {
        log.info { "Fetching games..." }
        val games = gameRepository.getAll(pageable, tenant)
        log.info { "Games fetched." }
        return games.map { game ->
            GameSummary(
                game = game,
                teamsCount = teamRepository.countByGame(game.id, tenant),
                agentsCount = agentRepository.countByGame(game.id, tenant),
            )
        }
    }

    override fun getGame(gameId: GameId, tenant: Tenant): GameSummary {
        log.info { "Fetching Game..." }
        val game = gameRepository.get(gameId, tenant)
        val teamsCount = teamRepository.countByGame(gameId, tenant)
        val agentsCount = agentRepository.countByGame(gameId, tenant)
        log.info { "Game fetched." }
        return GameSummary(game, teamsCount, agentsCount)
    }

    @Transactional
    override fun getMap(gameId: GameId, tenant: Tenant): Map {
        log.info { "Fetching map..." }
        val map = gameRepository.get(gameId, tenant).map
        log.info { "Map fetched." }
        return map
    }

    @Transactional
    override fun updateGame(command: GameUseCase.UpdateGameCommand, tenant: Tenant) {
        log.info { "Updating game..." }
        val game = gameRepository.get(command.gameId, tenant)

        command.title?.also {
            gameRepository.requireTitleAvailable(command.title, tenant)
            game.updateTitle(it)
        }

        command.endTime?.also { game.updateEndTime(it) }
        command.startTime?.also { game.updateStartTime(it) }
        command.map?.cornerA?.also { game.updateCornerA(it) }
        command.map?.cornerB?.also { game.updateCornerB(it) }
        command.map?.grid?.also { game.updateGrid(it) }

        gameRepository.save(game, tenant)

        log.info { "Game updated." }
    }

    @Transactional(readOnly = true)
    override fun getBoard(query: GameUseCase.GetBoardQuery, tenant: Tenant): Board {
        log.info { "Building board for game ${query.gameId} (team ${query.teamId})..." }

        val game = gameRepository.get(query.gameId, tenant)

        val foundAgentIds = query.teamId?.let { teamId ->
            teamRepository.getOrNull(teamId, tenant)
                ?.takeIf { it.gameId == query.gameId }
                ?: teamNotFound(teamId)
            findingRepository.byTeam(teamId, tenant)
                .map { it.agentId }
                .toSet()
        } ?: emptySet()

        val agents = agentRepository.forGame(query.gameId, tenant)
            .onEach { agent ->
                agentLocationRepository.latest(agent.id)?.also { agent.setLocation(it) }
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

    @Transactional(readOnly = true)
    override fun getLeaderboard(gameId: GameId, tenant: Tenant): Leaderboard {
        log.info { "Building leaderboard for game $gameId..." }

        val game = gameRepository.get(gameId, tenant)

        val scoringAgents = agentRepository.forGame(gameId, tenant)
            .filter { it.type == Agent.Type.MISTERX && it.active }
            .associateBy { it.id }

        val teams = teamRepository.forGame(gameId, Pageable.unpaged(), tenant).content

        val entries = teams.map { team ->
            val foundAgents = findingRepository.byTeam(team.id, tenant)
                .mapNotNull { finding ->
                    scoringAgents[finding.agentId]?.let { agent ->
                        FoundMisterX(agent.alias, finding.foundAt)
                    }
                }
                .sortedBy { it.foundAt }
            LeaderboardEntry(team, foundAgents)
        }

        log.info { "Leaderboard built: ${entries.size} teams ranked." }
        return Leaderboard.of(game, entries)
    }
}
