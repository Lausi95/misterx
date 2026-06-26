package net.lausi95.citygame.adapter.web.controller.agent

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import net.lausi95.citygame.adapter.`in`.web.TenantOriginExtractor
import net.lausi95.citygame.adapter.`in`.web.WebMvcConfig
import net.lausi95.citygame.adapter.`in`.web.controller.agent.AgentController
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.AgentNotFoundException
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.AgentUseCase
import net.lausi95.citygame.application.port.`in`.finding.FindingUseCase
import net.lausi95.citygame.adapter.`in`.web.FrontendUriFactory
import net.lausi95.citygame.common.Tenant
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete

@WebMvcTest(AgentController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TenantOriginExtractor::class, WebMvcConfig::class)
class AgentControllerDeleteTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var agentUseCase: AgentUseCase

    @MockkBean
    private lateinit var findingUseCase: FindingUseCase

    @MockkBean
    private lateinit var frontendUriFactory: FrontendUriFactory

    private val tenant = Tenant("https://acme.city-game.net")

    private fun deleteAgent(agentId: String = "a1") =
        mockMvc.delete("/games/g1/agents/$agentId") {
            header("Origin", "https://acme.city-game.net")
        }

    @Test
    fun `returns 204 and delegates the delete to the use case`() {
        every { agentUseCase.deleteAgent(any(), tenant) } just runs

        deleteAgent().andExpect {
            status { isNoContent() }
        }

        verify {
            agentUseCase.deleteAgent(
                AgentUseCase.DeleteAgentCommand(GameId("g1"), AgentId("a1")),
                tenant,
            )
        }
    }

    @Test
    fun `returns 404 when the agent belongs to a different game`() {
        every { agentUseCase.deleteAgent(any(), tenant) } throws
            AgentNotFoundException("Agent not found: a1")

        deleteAgent().andExpect {
            status { isNotFound() }
        }
    }
}
