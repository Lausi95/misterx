package net.lausi95.citygame.application.domain.service

import io.mockk.every
import io.mockk.mockk
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.AgentNotFoundException
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.GetMyAgentUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class GetMyAgentServiceTest {

    private val agentRepository = mockk<AgentRepository>()
    private val agentLocationRepository = mockk<AgentLocationRepository>()

    private val service = GetMyAgentService(agentRepository, agentLocationRepository)

    private val tenant = Tenant("https://acme.city-game.net")
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
        every { agentRepository.getOrNull(agentId, tenant) } returns anAgent()
        every { agentLocationRepository.latest(agentId) } returns null

        val result = service.getMyAgent(GetMyAgentUseCase.Query(gameId, agentId), tenant)

        assertThat(result.id).isEqualTo(agentId)
    }

    @Test
    fun `populates the last known location when present`() {
        val location = mockk<AgentLocation>()
        every { agentRepository.getOrNull(agentId, tenant) } returns anAgent()
        every { agentLocationRepository.latest(agentId) } returns location

        val result = service.getMyAgent(GetMyAgentUseCase.Query(gameId, agentId), tenant)

        assertThat(result.location).isEqualTo(location)
    }

    @Test
    fun `throws when the agent does not exist`() {
        every { agentRepository.getOrNull(agentId, tenant) } returns null

        assertThatThrownBy {
            service.getMyAgent(GetMyAgentUseCase.Query(gameId, agentId), tenant)
        }.isInstanceOf(AgentNotFoundException::class.java)
    }

    @Test
    fun `throws when the agent belongs to a different game`() {
        every { agentRepository.getOrNull(agentId, tenant) } returns anAgent(game = GameId("other"))

        assertThatThrownBy {
            service.getMyAgent(GetMyAgentUseCase.Query(gameId, agentId), tenant)
        }.isInstanceOf(AgentNotFoundException::class.java)
    }
}
