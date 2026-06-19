package net.lausi95.citygame.adapter.web.controller.team

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import net.lausi95.citygame.adapter.`in`.web.controller.team.TeamController
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamNotFoundException
import net.lausi95.citygame.application.port.`in`.finding.GetTeamFoundAgentsUseCase
import net.lausi95.citygame.application.port.`in`.team.CreateTeamUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamMembersUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamsUseCase
import net.lausi95.citygame.application.port.`in`.team.UpdateTeamUseCase
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

@WebMvcTest(TeamController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TeamControllerSetupQrTest.ImageConverterConfig::class)
class TeamControllerSetupQrTest {

    @TestConfiguration
    class ImageConverterConfig {
        @Bean
        fun bufferedImageHttpMessageConverter() = BufferedImageHttpMessageConverter()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var getTeamsUseCase: GetTeamsUseCase

    @MockkBean
    private lateinit var createTeamUseCase: CreateTeamUseCase

    @MockkBean
    private lateinit var getTeamUseCase: GetTeamUseCase

    @MockkBean
    private lateinit var updateTeamUseCase: UpdateTeamUseCase

    @MockkBean
    private lateinit var getTeamMembersUseCase: GetTeamMembersUseCase

    @MockkBean
    private lateinit var getTeamFoundAgentsUseCase: GetTeamFoundAgentsUseCase

    private fun getSetupQr(teamId: String = "t1") =
        mockMvc.get("/games/g1/teams/$teamId/setup-qr") {
            requestAttr("tenant", Tenant("acme"))
            accept(MediaType.IMAGE_PNG)
        }

    @Test
    fun `returns 200 with a PNG image for an existing team`() {
        every { getTeamUseCase.getTeam(TeamId("t1"), Tenant("acme")) } returns aTeam()

        getSetupQr().andExpect {
            status { isOk() }
            content { contentType(MediaType.IMAGE_PNG) }
        }
    }

    @Test
    fun `returns 404 when the team does not exist`() {
        every { getTeamUseCase.getTeam(TeamId("missing"), Tenant("acme")) } throws
            TeamNotFoundException("Team not found: missing")

        getSetupQr(teamId = "missing").andExpect {
            status { isNotFound() }
        }
    }

    private fun aTeam() = Team(
        id = TeamId("t1"),
        _gameId = GameId("g1"),
        _name = "Red Squad",
    )
}
