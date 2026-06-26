package net.lausi95.citygame.adapter.web.controller.find

import org.springframework.context.annotation.Import
import net.lausi95.citygame.adapter.`in`.web.WebMvcConfig
import net.lausi95.citygame.adapter.`in`.web.TenantOriginExtractor
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import net.lausi95.citygame.adapter.`in`.web.controller.find.FindController
import net.lausi95.citygame.application.domain.model.finding.AgentAlreadyFoundException
import net.lausi95.citygame.application.domain.model.finding.AgentNotFindableException
import net.lausi95.citygame.application.domain.model.finding.FindingId
import net.lausi95.citygame.application.domain.model.game.GameNotActiveException
import net.lausi95.citygame.application.domain.model.team.TeamNotFoundException
import net.lausi95.citygame.application.port.`in`.finding.FindingUseCase
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(FindController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TenantOriginExtractor::class, WebMvcConfig::class)
class FindControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var findingUseCase: FindingUseCase

    private fun post(body: String? = null) = mockMvc.post("/find") {
        header("X-GameId", "g1")
        header("X-TeamId", "t1")
        header("X-MemberId", "m1")
        header("X-AgentId", "a1")
        header("Origin", "https://acme.city-game.net")
        if (body != null) {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }
    }

    @Test
    fun `returns 201 with X-FindId and a Location pointing at the team`() {
        every { findingUseCase.findAgent(any(), any()) } returns FindingId("find-1")

        post().andExpect {
            status { isCreated() }
            header { string("X-FindId", "find-1") }
            header { string("Location", "http://localhost/games/g1/teams/t1") }
        }
    }

    @Test
    fun `passes the reported location from the body and the tenant to the use case`() {
        val command = slot<FindingUseCase.FindAgentCommand>()
        val tenant = slot<Tenant>()
        every { findingUseCase.findAgent(capture(command), capture(tenant)) } returns FindingId("find-1")

        post("""{"latitude":52.5,"longitude":13.4}""").andExpect { status { isCreated() } }

        assertThat(command.captured.gameId.value).isEqualTo("g1")
        assertThat(command.captured.teamId.value).isEqualTo("t1")
        assertThat(command.captured.memberId.value).isEqualTo("m1")
        assertThat(command.captured.agentId.value).isEqualTo("a1")
        assertThat(command.captured.reportedLocation?.latitude).isEqualTo(52.5)
        assertThat(command.captured.reportedLocation?.longitude).isEqualTo(13.4)
        assertThat(tenant.captured).isEqualTo(Tenant("https://acme.city-game.net"))
    }

    @Test
    fun `sends no reported location when the body is omitted`() {
        val command = slot<FindingUseCase.FindAgentCommand>()
        every { findingUseCase.findAgent(capture(command), any()) } returns FindingId("find-1")

        post().andExpect { status { isCreated() } }

        assertThat(command.captured.reportedLocation).isNull()
    }

    @Test
    fun `maps AgentNotFindableException to 422`() {
        every { findingUseCase.findAgent(any(), any()) } throws AgentNotFindableException("nope")

        post().andExpect { status { isUnprocessableContent() } }
    }

    @Test
    fun `maps GameNotActiveException to 422`() {
        every { findingUseCase.findAgent(any(), any()) } throws GameNotActiveException("nope")

        post().andExpect { status { isUnprocessableContent() } }
    }

    @Test
    fun `maps AgentAlreadyFoundException to 409`() {
        every { findingUseCase.findAgent(any(), any()) } throws AgentAlreadyFoundException("dup")

        post().andExpect { status { isConflict() } }
    }

    @Test
    fun `maps not-found domain exceptions to 404`() {
        every { findingUseCase.findAgent(any(), any()) } throws TeamNotFoundException("missing")

        post().andExpect { status { isNotFound() } }
    }
}
