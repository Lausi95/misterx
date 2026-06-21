package net.lausi95.citygame.adapter.web.controller.find

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import net.lausi95.citygame.adapter.`in`.web.FrontendUriFactory
import net.lausi95.citygame.adapter.`in`.web.controller.find.FindQrController
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.agentNotFound
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.GetMyAgentUseCase
import net.lausi95.citygame.common.Tenant
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(FindQrController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(FindQrControllerTest.ImageConverterConfig::class)
class FindQrControllerTest {

    @TestConfiguration
    class ImageConverterConfig {
        @Bean
        fun bufferedImageHttpMessageConverter() = BufferedImageHttpMessageConverter()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var getMyAgentUseCase: GetMyAgentUseCase

    @MockkBean
    private lateinit var frontendUriFactory: FrontendUriFactory

    private fun getFindQr(gameId: String = "g1", agentId: String = "a1") =
        mockMvc.get("/find-qr") {
            header("X-GameId", gameId)
            header("X-AgentId", agentId)
            requestAttr("tenant", Tenant("acme"))
            accept(MediaType.IMAGE_PNG)
        }

    @Test
    fun `returns 200 with a PNG image for an agent that belongs to the game`() {
        every {
            getMyAgentUseCase.getMyAgent(GetMyAgentUseCase.Query(GameId("g1"), AgentId("a1")), Tenant("acme"))
        } returns anAgent()
        every {
            frontendUriFactory.buildUrl("/find", mapOf("agentId" to "a1", "alias" to "Shadow"))
        } returns "https://foo.city-game.net/find?agentId=a1&alias=Shadow"

        getFindQr().andExpect {
            status { isOk() }
            content { contentType(MediaType.IMAGE_PNG) }
        }
    }

    @Test
    fun `returns 404 when the agent does not belong to the game`() {
        every {
            getMyAgentUseCase.getMyAgent(GetMyAgentUseCase.Query(GameId("g1"), AgentId("missing")), Tenant("acme"))
        } answers { agentNotFound(AgentId("missing")) }

        getFindQr(agentId = "missing").andExpect {
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
