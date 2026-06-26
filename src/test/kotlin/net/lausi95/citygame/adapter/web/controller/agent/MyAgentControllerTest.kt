package net.lausi95.citygame.adapter.web.controller.agent

import org.springframework.context.annotation.Import
import net.lausi95.citygame.adapter.`in`.web.WebMvcConfig
import net.lausi95.citygame.adapter.`in`.web.TenantOriginExtractor
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import net.lausi95.citygame.adapter.`in`.web.controller.agent.MyAgentController
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.AgentNotFoundException
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.AgentUseCase
import net.lausi95.citygame.application.port.`in`.finding.FindingUseCase
import net.lausi95.citygame.common.Tenant
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(MyAgentController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TenantOriginExtractor::class, WebMvcConfig::class)
class MyAgentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var agentUseCase: AgentUseCase

    @MockkBean
    private lateinit var findingUseCase: FindingUseCase

    private val tenant = Tenant("https://acme.city-game.net")

    private fun getMyAgent() =
        mockMvc.get("/my-agent") {
            header("Origin", tenant.value)
            header("X-GameId", "g1")
            header("X-AgentId", "a1")
        }

    private fun anAgent() = Agent(
        id = AgentId("a1"),
        _gameId = GameId("g1"),
        _type = Agent.Type.MISTERX,
        _phoneNumber = "+49123",
        _firstName = "Jane",
        _lastName = "Doe",
        _alias = "Shadow",
        _active = true,
    )

    @Test
    fun `returns 200 with the agent for a valid game and agent`() {
        every {
            agentUseCase.getMyAgent(AgentUseCase.GetMyAgentQuery(GameId("g1"), AgentId("a1")), tenant)
        } returns anAgent()
        every { findingUseCase.getFindingTeams(AgentId("a1"), tenant) } returns emptyList()

        getMyAgent().andExpect {
            status { isOk() }
            jsonPath("$.id") { value("a1") }
            jsonPath("$.alias") { value("Shadow") }
            jsonPath("$.type") { value("MISTERX") }
        }
    }

    @Test
    fun `returns 404 when the agent is not found`() {
        every {
            agentUseCase.getMyAgent(AgentUseCase.GetMyAgentQuery(GameId("g1"), AgentId("a1")), tenant)
        } throws AgentNotFoundException("Agent not found: a1")

        getMyAgent().andExpect {
            status { isNotFound() }
        }
    }
}
