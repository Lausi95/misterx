package net.lausi95.citygame.application.domain.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.AgentNotFoundException
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
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime

class AgentServiceTest {

    private val tenant = Tenant("https://acme.city-game.net")

    @Nested
    inner class DeleteAgent {

        private val agentRepository = mockk<AgentRepository>(relaxed = true)
        private val findingRepository = mockk<FindingRepository>(relaxed = true)
        private val agentLocationRepository = mockk<AgentLocationRepository>(relaxed = true)
        private val service = AgentService(
            mockk(relaxed = true),
            agentRepository,
            agentLocationRepository,
            findingRepository,
        )

        private val gameId = GameId()
        private val agentId = AgentId()

        private fun agent(gameId: GameId = this.gameId) =
            Agent(agentId, gameId, Agent.Type.MISTERX, "phone", "first", "last", "alias", true)

        private fun command() = AgentUseCase.DeleteAgentCommand(gameId, agentId)

        @Test
        fun `cascades findings, locations and the agent in order`() {
            every { agentRepository.getOrNull(agentId, tenant) } returns agent()

            service.deleteAgent(command(), tenant)

            verifyOrder {
                findingRepository.deleteByAgent(agentId, tenant)
                agentLocationRepository.deleteByAgent(agentId, tenant)
                agentRepository.delete(agentId, tenant)
            }
        }

        @Test
        fun `is idempotent - does nothing when the agent does not exist`() {
            every { agentRepository.getOrNull(agentId, tenant) } returns null

            service.deleteAgent(command(), tenant)

            verify(exactly = 0) { findingRepository.deleteByAgent(any(), any()) }
            verify(exactly = 0) { agentLocationRepository.deleteByAgent(any(), any()) }
            verify(exactly = 0) { agentRepository.delete(any(), any()) }
        }

        @Test
        fun `rejects deletion when the agent belongs to a different game`() {
            every { agentRepository.getOrNull(agentId, tenant) } returns agent(gameId = GameId())

            assertThatThrownBy { service.deleteAgent(command(), tenant) }
                .isInstanceOf(AgentNotFoundException::class.java)

            verify(exactly = 0) { findingRepository.deleteByAgent(any(), any()) }
            verify(exactly = 0) { agentLocationRepository.deleteByAgent(any(), any()) }
            verify(exactly = 0) { agentRepository.delete(any(), any()) }
        }
    }

    @Nested
    inner class GetAgents {

        private val agentRepository = mockk<AgentRepository>()
        private val service = AgentService(
            mockk(relaxed = true),
            agentRepository,
            mockk(relaxed = true),
            mockk(relaxed = true),
        )

        private val gameId = GameId()
        private val now: OffsetDateTime = OffsetDateTime.parse("2026-06-22T12:00:00Z")

        private fun agent(alias: String, id: AgentId = AgentId()) =
            Agent(id, gameId, Agent.Type.UTILITY, "phone", "first", "last", alias, true)

        private fun givenAgents(vararg agents: Agent) {
            every { agentRepository.forGameWithLocation(gameId, tenant) } returns agents.toList()
        }

        @Suppress("UnusedParameter")
        private fun withoutLocation(agent: Agent) = Unit

        private fun locatedAt(agent: Agent, timestamp: OffsetDateTime) {
            agent.setLocation(AgentLocation(AgentLocationId(), agent.id, timestamp, GeoLocation(0.0, 0.0)))
        }

        private fun getAgents(pageable: Pageable = Pageable.unpaged()) =
            service.getAgents(gameId, pageable, tenant).content.map { it.alias }

        @Test
        fun `never-located agents come first, oldest location next, most recent last`() {
            val recent = agent("recent")
            val old = agent("old")
            val never = agent("never")
            locatedAt(recent, now.minusMinutes(5))
            locatedAt(old, now.minusHours(3))
            givenAgents(recent, old, never)

            assertThat(getAgents()).containsExactly("never", "old", "recent")
        }

        @Test
        fun `multiple never-located agents are ordered by alias, case-insensitively`() {
            val charlie = agent("charlie")
            val alpha = agent("Alpha")
            val bravo = agent("bravo")
            givenAgents(charlie, alpha, bravo)

            assertThat(getAgents()).containsExactly("Alpha", "bravo", "charlie")
        }

        @Test
        fun `aliases are ordered German-correct - umlauts sort next to their base letter`() {
            val zoo = agent("Zoo")
            val arger = agent("Ärger")
            val apfel = agent("Apfel")
            givenAgents(zoo, arger, apfel)

            assertThat(getAgents()).containsExactly("Apfel", "Ärger", "Zoo")
        }

        @Test
        fun `agents are ordered by agent id when staleness and alias are equal`() {
            val first = agent("same", AgentId("00000000-0000-0000-0000-000000000001"))
            val second = agent("same", AgentId("00000000-0000-0000-0000-000000000002"))
            givenAgents(second, first)

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
            locatedAt(recent, now.minusMinutes(1))
            locatedAt(old, now.minusHours(2))
            givenAgents(recent, old)

            val order = getAgents(PageRequest.of(0, 10, Sort.by("alias").ascending()))
            assertThat(order).containsExactly("zzz", "aaa")
        }

        @Test
        fun `pages are sliced in memory while reporting the full total`() {
            val never = agent("never")
            val old = agent("old")
            val recent = agent("recent")
            locatedAt(old, now.minusHours(1))
            locatedAt(recent, now.minusMinutes(1))
            givenAgents(recent, old, never)

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

    @Nested
    inner class GetMyAgent {

        private val agentRepository = mockk<AgentRepository>()
        private val service = AgentService(
            mockk(relaxed = true),
            agentRepository,
            mockk(relaxed = true),
            mockk(relaxed = true),
        )

        private val gameId = GameId("g1")
        private val agentId = AgentId("a1")

        private fun anAgent(game: GameId = gameId) = Agent(
            id = agentId,
            _gameId = game,
            _type = Agent.Type.MISTERX,
            _phoneNumber = "+49123",
            _firstName = "Jane",
            _lastName = "Doe",
            _alias = "Shadow",
            _active = true,
        )

        @Test
        fun `returns the agent for a valid game and agent`() {
            every { agentRepository.getWithLocation(agentId, tenant) } returns anAgent()

            val result = service.getMyAgent(AgentUseCase.GetMyAgentQuery(gameId, agentId), tenant)

            assertThat(result.id).isEqualTo(agentId)
        }

        @Test
        fun `populates the last known location when present`() {
            val location = mockk<AgentLocation>()
            every { agentRepository.getWithLocation(agentId, tenant) } returns anAgent().also { it.setLocation(location) }

            val result = service.getMyAgent(AgentUseCase.GetMyAgentQuery(gameId, agentId), tenant)

            assertThat(result.location).isEqualTo(location)
        }

        @Test
        fun `throws when the agent does not exist`() {
            every { agentRepository.getWithLocation(agentId, tenant) } throws AgentNotFoundException("Agent not found")

            assertThatThrownBy {
                service.getMyAgent(AgentUseCase.GetMyAgentQuery(gameId, agentId), tenant)
            }.isInstanceOf(AgentNotFoundException::class.java)
        }

        @Test
        fun `throws when the agent belongs to a different game`() {
            every { agentRepository.getWithLocation(agentId, tenant) } returns anAgent(game = GameId("other"))

            assertThatThrownBy {
                service.getMyAgent(AgentUseCase.GetMyAgentQuery(gameId, agentId), tenant)
            }.isInstanceOf(AgentNotFoundException::class.java)
        }
    }
}
