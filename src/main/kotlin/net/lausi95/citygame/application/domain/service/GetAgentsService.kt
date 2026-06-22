package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.GetAgentsUseCase
import net.lausi95.citygame.application.port.out.agent.GetAgentsPort
import net.lausi95.citygame.application.port.out.agentlocation.GetAgentLocationPort
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

private val log = KotlinLogging.logger { }

@Service
class GetAgentsService(
    private val getAgentsPort: GetAgentsPort,
    private val getAgentLocationPort: GetAgentLocationPort
) : GetAgentsUseCase {

    /**
     * Ordering is by **location staleness** (see ADR 0014): never-located agents first
     * (infinitely stale), then the oldest last-known location, ties broken by alias and
     * finally agent id for a stable total order. `now` is constant within a request, so
     * staleness is just the location timestamp ascending, nulls first — no age is computed.
     */
    private val byLocationStaleness: Comparator<Agent> =
        compareBy<Agent, OffsetDateTime?>(nullsFirst()) { it.location?.timestamp }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.alias }
            .thenBy { it.id.value }

    override fun getAgents(
        gameId: GameId,
        pageable: Pageable,
        tenant: Tenant
    ): Page<Agent> {
        log.info { "Fetching agents..." }

        // The sort key (location staleness) is not a column on the agent and is enriched only
        // after load, so the database cannot order or page on it. The agent count per game is
        // small and bounded (~20, ADR 0014), so we fetch all, enrich, order and page in memory.
        val agents = getAgentsPort.getAgentsForGame(gameId, tenant)
            .onEach { agent ->
                getAgentLocationPort.getAgentLocation(agent.id)?.also { agent.setLocation(it) }
            }
            .sortedWith(byLocationStaleness)

        log.info { "Agents fetched." }
        return agents.toPage(pageable)
    }

    private fun List<Agent>.toPage(pageable: Pageable): Page<Agent> {
        if (pageable.isUnpaged) return PageImpl(this)
        val from = pageable.offset.toInt().coerceAtMost(size)
        val to = (from + pageable.pageSize).coerceAtMost(size)
        return PageImpl(subList(from, to), pageable, size.toLong())
    }
}
