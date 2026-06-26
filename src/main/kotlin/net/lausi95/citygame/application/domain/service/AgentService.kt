package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.agentNotFound
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocationId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.AgentUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.Collator
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Locale

private val log = KotlinLogging.logger { }

@Service
class AgentService(
    private val gameRepository: GameRepository,
    private val agentRepository: AgentRepository,
    private val agentLocationRepository: AgentLocationRepository,
    private val findingRepository: FindingRepository,
) : AgentUseCase {

    /**
     * German, case-insensitive ordering of aliases: folds case and places umlauts next to
     * their base letter, mirroring the `de-DE-x-icu` collation the teams list uses (ADR 0015).
     * `SECONDARY` strength keeps accents significant but ignores case.
     */
    private val germanCollator: Collator =
        Collator.getInstance(Locale.GERMAN).apply { strength = Collator.SECONDARY }

    private val byAlias: Comparator<String> = Comparator { a, b -> germanCollator.compare(a, b) }

    /**
     * Ordering is by **location staleness** (see ADR 0014): never-located agents first
     * (infinitely stale), then the oldest last-known location, ties broken by alias and
     * finally agent id for a stable total order.
     */
    private val byLocationStaleness: Comparator<Agent> =
        compareBy<Agent, OffsetDateTime?>(nullsFirst()) { it.location?.timestamp }
            .thenBy(byAlias) { it.alias }
            .thenBy { it.id.value }

    @Transactional
    override fun createAgent(command: AgentUseCase.CreateAgentCommand, tenant: Tenant): AgentId {
        log.info { "Creating new agent..." }

        gameRepository.requireExists(command.gameId, tenant)

        val agent = Agent(
            AgentId(),
            command.gameId,
            command.type,
            command.phoneNumber,
            command.firstName,
            command.lastName,
            command.alias,
            command.active,
        )

        agentRepository.save(agent, tenant)

        log.info { "Agent created." }

        return agent.id
    }

    override fun getAgents(gameId: GameId, pageable: Pageable, tenant: Tenant): Page<Agent> {
        log.info { "Fetching agents..." }

        val agents = agentRepository.forGameWithLocation(gameId, tenant)
            .sortedWith(byLocationStaleness)

        log.info { "Agents fetched." }
        return agents.toPage(pageable)
    }

    override fun getAgent(agentId: AgentId, tenant: Tenant): Agent {
        log.info { "Fetching agent..." }

        val agent = agentRepository.getWithLocation(agentId, tenant)

        log.info { "Agent fetched." }

        return agent
    }

    override fun getMyAgent(query: AgentUseCase.GetMyAgentQuery, tenant: Tenant): Agent {
        log.info { "Fetching my agent..." }

        val agent = agentRepository.getWithLocation(query.agentId, tenant)
        if (agent.gameId != query.gameId) agentNotFound(query.agentId)

        log.info { "My agent fetched." }

        return agent
    }

    override fun updateAgent(command: AgentUseCase.UpdateAgentCommand, tenant: Tenant) {
        log.info { "Updating Agent..." }

        val agent = agentRepository.get(command.agentId, tenant)

        command.type?.also { agent.updateType(it) }
        command.firstName?.also { agent.updateFirstName(it) }
        command.lastName?.also { agent.updateLastName(it) }
        command.phoneNumber?.also { agent.updatePhoneNumber(it) }
        command.alias?.also { agent.updateAlias(it) }
        command.active?.also { agent.updateActive(it) }

        agentRepository.save(agent, tenant)

        log.info { "Agent updated." }
    }

    @Transactional
    override fun deleteAgent(command: AgentUseCase.DeleteAgentCommand, tenant: Tenant) {
        log.info { "Deleting agent ${command.agentId.value}..." }

        val agent = agentRepository.getOrNull(command.agentId, tenant) ?: return

        if (agent.gameId != command.gameId) {
            agentNotFound(command.agentId)
        }

        findingRepository.deleteByAgent(command.agentId, tenant)
        agentLocationRepository.deleteByAgent(command.agentId, tenant)
        agentRepository.delete(command.agentId, tenant)

        log.info { "Agent ${command.agentId.value} deleted." }
    }

    override fun updateLocation(gameId: GameId, agentId: AgentId, geoLocation: GeoLocation, tenant: Tenant) {
        log.info { "Updating agent location..." }

        gameRepository.requireExists(gameId, tenant)
        agentRepository.requireExists(agentId, tenant)

        val agentLocation = AgentLocation(
            AgentLocationId(),
            agentId,
            OffsetDateTime.now(ZoneOffset.UTC),
            geoLocation,
        )

        agentLocationRepository.save(agentLocation, tenant)

        log.info { "Agent location updated" }
    }

    private fun List<Agent>.toPage(pageable: Pageable): Page<Agent> {
        if (pageable.isUnpaged) return PageImpl(this)
        val from = pageable.offset.toInt().coerceAtMost(size)
        val to = (from + pageable.pageSize).coerceAtMost(size)
        return PageImpl(subList(from, to), pageable, size.toLong())
    }
}
