package net.lausi95.citygame.adapter.web.controller.team

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import net.lausi95.citygame.adapter.`in`.web.controller.team.MyTeamController
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.FoundAgent
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.application.domain.model.team.TeamMemberNotFoundException
import net.lausi95.citygame.application.domain.model.team.TeamNotFoundException
import net.lausi95.citygame.application.port.`in`.finding.GetTeamFoundAgentsUseCase
import net.lausi95.citygame.application.port.`in`.team.GetMyTeamUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamMembersUseCase
import net.lausi95.citygame.common.Tenant
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.ZonedDateTime

@WebMvcTest(MyTeamController::class)
@AutoConfigureMockMvc(addFilters = false)
class MyTeamControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var getMyTeamUseCase: GetMyTeamUseCase

    @MockkBean
    private lateinit var getTeamMembersUseCase: GetTeamMembersUseCase

    @MockkBean
    private lateinit var getTeamFoundAgentsUseCase: GetTeamFoundAgentsUseCase

    private val tenant = Tenant("acme")

    private fun getMyTeam(memberId: String? = null) =
        mockMvc.get("/my-team") {
            requestAttr("tenant", tenant)
            header("X-GameId", "g1")
            header("X-TeamId", "t1")
            memberId?.let { header("X-MemberId", it) }
        }

    private fun aTeam() = Team(id = TeamId("t1"), _gameId = GameId("g1"), _name = "Red Squad")

    @Test
    fun `returns 200 with the team for a valid game and team`() {
        every {
            getMyTeamUseCase.getMyTeam(GetMyTeamUseCase.Query(GameId("g1"), TeamId("t1"), null), tenant)
        } returns aTeam()
        every { getTeamMembersUseCase.countTeamMembers(TeamId("t1"), tenant) } returns 3L
        every { getTeamFoundAgentsUseCase.getFoundAgents(TeamId("t1"), tenant) } returns emptyList()

        getMyTeam().andExpect {
            status { isOk() }
            jsonPath("$.id") { value("t1") }
            jsonPath("$.name") { value("Red Squad") }
            jsonPath("$.memberCount") { value(3) }
        }
    }

    @Test
    fun `serialises foundAt for each found agent in the response body`() {
        val foundAt = ZonedDateTime.parse("2026-06-19T10:15:30+02:00")
        every {
            getMyTeamUseCase.getMyTeam(GetMyTeamUseCase.Query(GameId("g1"), TeamId("t1"), null), tenant)
        } returns aTeam()
        every { getTeamMembersUseCase.countTeamMembers(TeamId("t1"), tenant) } returns 3L
        every { getTeamFoundAgentsUseCase.getFoundAgents(TeamId("t1"), tenant) } returns
            listOf(FoundAgent(AgentId("a1"), "Shadow", foundAt))

        getMyTeam().andExpect {
            status { isOk() }
            jsonPath("$.foundAgents[0].id") { value("a1") }
            jsonPath("$.foundAgents[0].name") { value("Shadow") }
            jsonPath("$.foundAgents[0].foundAt") { exists() }
        }
    }

    @Test
    fun `passes the member id through when supplied`() {
        every {
            getMyTeamUseCase.getMyTeam(
                GetMyTeamUseCase.Query(GameId("g1"), TeamId("t1"), TeamMemberId("m1")),
                tenant,
            )
        } returns aTeam()
        every { getTeamMembersUseCase.countTeamMembers(TeamId("t1"), tenant) } returns 1L
        every { getTeamFoundAgentsUseCase.getFoundAgents(TeamId("t1"), tenant) } returns emptyList()

        getMyTeam(memberId = "m1").andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `returns 404 when the team is not found`() {
        every {
            getMyTeamUseCase.getMyTeam(GetMyTeamUseCase.Query(GameId("g1"), TeamId("t1"), null), tenant)
        } throws TeamNotFoundException("Team not found: t1")

        getMyTeam().andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `returns 404 when the supplied member is invalid`() {
        every {
            getMyTeamUseCase.getMyTeam(
                GetMyTeamUseCase.Query(GameId("g1"), TeamId("t1"), TeamMemberId("m1")),
                tenant,
            )
        } throws TeamMemberNotFoundException("Team member not found: m1")

        getMyTeam(memberId = "m1").andExpect {
            status { isNotFound() }
        }
    }
}
