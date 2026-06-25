package net.lausi95.citygame.application.domain.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.AgentNotFoundException
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.DeleteAgentUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DeleteAgentServiceTest {

    private val agentRepository = mockk<AgentRepository>(relaxed = true)
    private val findingRepository = mockk<FindingRepository>(relaxed = true)
    private val agentLocationRepository = mockk<AgentLocationRepository>(relaxed = true)
    private val service = DeleteAgentService(
        agentRepository,
        findingRepository,
        agentLocationRepository,
    )

    private val tenant = Tenant("https://acme.city-game.net")
    private val gameId = GameId()
    private val agentId = AgentId()

    private fun agent(gameId: GameId = this.gameId) =
        Agent(agentId, gameId, Agent.Type.MISTERX, "phone", "first", "last", "alias", true)

    private fun command() = DeleteAgentUseCase.Command(gameId, agentId)

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
