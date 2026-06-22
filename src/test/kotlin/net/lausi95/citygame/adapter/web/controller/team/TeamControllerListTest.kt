package net.lausi95.citygame.adapter.web.controller.team

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import net.lausi95.citygame.adapter.`in`.web.FrontendUriFactory
import net.lausi95.citygame.adapter.`in`.web.TenantOriginExtractor
import net.lausi95.citygame.adapter.`in`.web.WebMvcConfig
import net.lausi95.citygame.adapter.`in`.web.controller.team.TeamController
import net.lausi95.citygame.application.port.`in`.finding.GetTeamFoundAgentsUseCase
import net.lausi95.citygame.application.port.`in`.team.CreateTeamUseCase
import net.lausi95.citygame.application.port.`in`.team.DeleteTeamUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamMembersUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamsUseCase
import net.lausi95.citygame.application.port.`in`.team.UpdateTeamUseCase
import net.lausi95.citygame.application.domain.model.team.Team
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(TeamController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TenantOriginExtractor::class, WebMvcConfig::class)
class TeamControllerListTest {

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
    private lateinit var deleteTeamUseCase: DeleteTeamUseCase

    @MockkBean
    private lateinit var getTeamMembersUseCase: GetTeamMembersUseCase

    @MockkBean
    private lateinit var getTeamFoundAgentsUseCase: GetTeamFoundAgentsUseCase

    @MockkBean
    private lateinit var frontendUriFactory: FrontendUriFactory

    @Test
    fun `defaults to sorting by name ascending when the client sends no sort`() {
        val pageable = slot<Pageable>()
        every { getTeamsUseCase.getTeams(any(), capture(pageable), any()) } returns PageImpl(emptyList<Team>())

        mockMvc.get("/games/g1/teams") {
            header("Origin", "https://acme.city-game.net")
        }.andExpect {
            status { isOk() }
        }

        val order = pageable.captured.sort.getOrderFor("name")
        assertThat(order).isNotNull
        assertThat(order!!.direction).isEqualTo(Sort.Direction.ASC)
    }
}
