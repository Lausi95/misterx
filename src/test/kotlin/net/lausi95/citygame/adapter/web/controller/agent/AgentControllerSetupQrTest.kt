package net.lausi95.citygame.adapter.web.controller.agent

import net.lausi95.citygame.adapter.`in`.web.WebMvcConfig
import net.lausi95.citygame.adapter.`in`.web.TenantOriginExtractor
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import net.lausi95.citygame.adapter.`in`.web.FrontendUriFactory
import net.lausi95.citygame.adapter.`in`.web.controller.agent.AgentController
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.AgentNotFoundException
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.CreateAgentUseCase
import net.lausi95.citygame.application.port.`in`.agent.GetAgentUseCase
import net.lausi95.citygame.application.port.`in`.agent.GetAgentsUseCase
import net.lausi95.citygame.application.port.`in`.agent.UpdateAgentUseCase
import net.lausi95.citygame.application.port.`in`.finding.GetAgentFindingTeamsUseCase
import net.lausi95.citygame.common.Tenant
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(AgentController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AgentControllerSetupQrTest.ImageConverterConfig::class, TenantOriginExtractor::class, WebMvcConfig::class)
class AgentControllerSetupQrTest {

    @TestConfiguration
    class ImageConverterConfig {
        @Bean
        fun bufferedImageHttpMessageConverter() = BufferedImageHttpMessageConverter()
    }

    @org.springframework.beans.factory.annotation.Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var getAgentsUseCase: GetAgentsUseCase

    @MockkBean
    private lateinit var createAgentUseCase: CreateAgentUseCase

    @MockkBean
    private lateinit var getAgentUseCase: GetAgentUseCase

    @MockkBean
    private lateinit var updateAgentUseCase: UpdateAgentUseCase

    @MockkBean
    private lateinit var getAgentFindingTeamsUseCase: GetAgentFindingTeamsUseCase

    @MockkBean
    private lateinit var frontendUriFactory: FrontendUriFactory

    private fun getSetupQr(agentId: String = "a1") =
        mockMvc.get("/games/g1/agents/$agentId/setup-qr") {
            header("Origin", "https://acme.city-game.net")
            accept(MediaType.IMAGE_PNG)
        }

    @Test
    fun `returns 200 with a PNG image for an existing agent`() {
        every { getAgentUseCase.getAgent(AgentId("a1"), Tenant("https://acme.city-game.net")) } returns anAgent()
        every { frontendUriFactory.buildUrl(any(), any(), any()) } returns "http://localhost:3000/setup-agent?agentId=a1"

        getSetupQr().andExpect {
            status { isOk() }
            content { contentType(MediaType.IMAGE_PNG) }
        }
    }

    @Test
    fun `returns 404 when the agent does not exist`() {
        every { getAgentUseCase.getAgent(AgentId("missing"), Tenant("https://acme.city-game.net")) } throws
            AgentNotFoundException("Agent not found: missing")

        getSetupQr(agentId = "missing").andExpect {
            status { isNotFound() }
        }
    }

    private fun anAgent() = Agent(
        id = AgentId("a1"),
        _gameId = GameId("g1"),
        _type = Agent.Type.MISTERX,
        _phoneNumber = "+49123456789",
        _firstName = "Jane",
        _lastName = "Doe",
        _alias = "Shadow",
        _active = true,
    )
}
