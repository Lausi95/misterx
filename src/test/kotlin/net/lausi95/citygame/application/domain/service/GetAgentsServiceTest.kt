package net.lausi95.citygame.application.domain.service

import io.mockk.every
import io.mockk.mockk
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocationId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.out.agent.GetAgentsPort
import net.lausi95.citygame.application.port.out.agentlocation.GetAgentLocationPort
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime

class GetAgentsServiceTest {

    private val getAgentsPort = mockk<GetAgentsPort>()
    private val getAgentLocationPort = mockk<GetAgentLocationPort>()
    private val service = GetAgentsService(getAgentsPort, getAgentLocationPort)

    private val tenant = Tenant("https://acme.city-game.net")
    private val gameId = GameId()
    private val now: OffsetDateTime = OffsetDateTime.parse("2026-06-22T12:00:00Z")

    private fun agent(alias: String, id: AgentId = AgentId()) =
        Agent(id, gameId, Agent.Type.UTILITY, "phone", "first", "last", alias, true)

    private fun givenAgents(vararg agents: Agent) {
        every { getAgentsPort.getAgentsForGame(gameId, tenant) } returns agents.toList()
    }

    /** No location at all → infinitely stale. */
    private fun withoutLocation(agent: Agent) {
        every { getAgentLocationPort.getAgentLocation(agent.id) } returns null
    }

    private fun locatedAt(agent: Agent, timestamp: OffsetDateTime) {
        every { getAgentLocationPort.getAgentLocation(agent.id) } returns
            AgentLocation(AgentLocationId(), agent.id, timestamp, GeoLocation(0.0, 0.0))
    }

    private fun getAgents(pageable: Pageable = Pageable.unpaged()) =
        service.getAgents(gameId, pageable, tenant).content.map { it.alias }

    @Test
    fun `never-located agents come first, oldest location next, most recent last`() {
        val recent = agent("recent")
        val old = agent("old")
        val never = agent("never")
        givenAgents(recent, old, never)
        locatedAt(recent, now.minusMinutes(5))
        locatedAt(old, now.minusHours(3))
        withoutLocation(never)

        assertThat(getAgents()).containsExactly("never", "old", "recent")
    }

    @Test
    fun `multiple never-located agents are ordered by alias, case-insensitively`() {
        val charlie = agent("charlie")
        val alpha = agent("Alpha")
        val bravo = agent("bravo")
        givenAgents(charlie, alpha, bravo)
        listOf(charlie, alpha, bravo).forEach { withoutLocation(it) }

        assertThat(getAgents()).containsExactly("Alpha", "bravo", "charlie")
    }

    @Test
    fun `aliases are ordered German-correct - umlauts sort next to their base letter`() {
        // ASCII ordering would place "Ärger" (code point 0xC4) after "Zoo"; German collation
        // folds it next to "A", so the expected order is Apfel, Ärger, Zoo.
        val zoo = agent("Zoo")
        val arger = agent("Ärger")
        val apfel = agent("Apfel")
        givenAgents(zoo, arger, apfel)
        listOf(zoo, arger, apfel).forEach { withoutLocation(it) }

        assertThat(getAgents()).containsExactly("Apfel", "Ärger", "Zoo")
    }

    @Test
    fun `agents are ordered by agent id when staleness and alias are equal`() {
        val first = agent("same", AgentId("00000000-0000-0000-0000-000000000001"))
        val second = agent("same", AgentId("00000000-0000-0000-0000-000000000002"))
        givenAgents(second, first)
        listOf(first, second).forEach { withoutLocation(it) }

        val ids = service.getAgents(gameId, Pageable.unpaged(), tenant).content.map { it.id.value }
        assertThat(ids).containsExactly(
            "00000000-0000-0000-0000-000000000001",
            "00000000-0000-0000-0000-000000000002",
        )
    }

    @Test
    fun `the client-supplied sort is ignored - the staleness order is always imposed`() {
        val recent = agent("aaa")
        val old = agent("zzz")
        givenAgents(recent, old)
        locatedAt(recent, now.minusMinutes(1))
        locatedAt(old, now.minusHours(2))

        // Ask for alias ascending; expect staleness order (oldest "zzz" first) regardless.
        val order = getAgents(PageRequest.of(0, 10, Sort.by("alias").ascending()))
        assertThat(order).containsExactly("zzz", "aaa")
    }

    @Test
    fun `pages are sliced in memory while reporting the full total`() {
        val never = agent("never")
        val old = agent("old")
        val recent = agent("recent")
        givenAgents(recent, old, never)
        withoutLocation(never)
        locatedAt(old, now.minusHours(1))
        locatedAt(recent, now.minusMinutes(1))

        val secondPage = service.getAgents(gameId, PageRequest.of(1, 2), tenant)

        assertThat(secondPage.content.map { it.alias }).containsExactly("recent")
        assertThat(secondPage.totalElements).isEqualTo(3)
        assertThat(secondPage.totalPages).isEqualTo(2)
        assertThat(secondPage.number).isEqualTo(1)
    }

    @Test
    fun `an empty game yields an empty page`() {
        givenAgents()

        val page = service.getAgents(gameId, PageRequest.of(0, 20), tenant)

        assertThat(page.content).isEmpty()
        assertThat(page.totalElements).isEqualTo(0)
    }
}
