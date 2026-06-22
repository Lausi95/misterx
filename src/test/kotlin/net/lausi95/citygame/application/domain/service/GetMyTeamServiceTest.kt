package net.lausi95.citygame.application.domain.service

import io.mockk.every
import io.mockk.mockk
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.application.domain.model.team.TeamMemberNotFoundException
import net.lausi95.citygame.application.domain.model.team.TeamNotFoundException
import net.lausi95.citygame.application.port.`in`.team.GetMyTeamUseCase
import net.lausi95.citygame.application.port.out.team.GetTeamMemberPort
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class GetMyTeamServiceTest {

    private val getTeamPort = mockk<GetTeamPort>()
    private val getTeamMemberPort = mockk<GetTeamMemberPort>()

    private val service = GetMyTeamService(getTeamPort, getTeamMemberPort)

    private val tenant = Tenant("https://acme.city-game.net")
    private val gameId = GameId("g1")
    private val teamId = TeamId("t1")
    private val memberId = TeamMemberId("m1")

    private fun aTeam(game: GameId = gameId) = Team(id = teamId, _gameId = game, _name = "Red Squad")

    private fun aMember(team: TeamId = teamId, game: GameId = gameId) =
        TeamMember(id = memberId, teamId = team, gameId = game, registeredAt = OffsetDateTime.parse("1970-01-01T00:00:00Z"))

    @Test
    fun `returns the team when no member id is supplied`() {
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns aTeam()

        val result = service.getMyTeam(GetMyTeamUseCase.Query(gameId, teamId, null), tenant)

        assertThat(result.id).isEqualTo(teamId)
    }

    @Test
    fun `throws when the team does not exist`() {
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns null

        assertThatThrownBy {
            service.getMyTeam(GetMyTeamUseCase.Query(gameId, teamId, null), tenant)
        }.isInstanceOf(TeamNotFoundException::class.java)
    }

    @Test
    fun `throws when the team belongs to a different game`() {
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns aTeam(game = GameId("other"))

        assertThatThrownBy {
            service.getMyTeam(GetMyTeamUseCase.Query(gameId, teamId, null), tenant)
        }.isInstanceOf(TeamNotFoundException::class.java)
    }

    @Test
    fun `returns the team when the member exists and belongs to this team and game`() {
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns aTeam()
        every { getTeamMemberPort.getTeamMemberOrNull(memberId, tenant) } returns aMember()

        val result = service.getMyTeam(GetMyTeamUseCase.Query(gameId, teamId, memberId), tenant)

        assertThat(result.id).isEqualTo(teamId)
    }

    @Test
    fun `throws when the supplied member does not exist`() {
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns aTeam()
        every { getTeamMemberPort.getTeamMemberOrNull(memberId, tenant) } returns null

        assertThatThrownBy {
            service.getMyTeam(GetMyTeamUseCase.Query(gameId, teamId, memberId), tenant)
        }.isInstanceOf(TeamMemberNotFoundException::class.java)
    }

    @Test
    fun `throws when the supplied member belongs to a different team`() {
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns aTeam()
        every { getTeamMemberPort.getTeamMemberOrNull(memberId, tenant) } returns aMember(team = TeamId("other"))

        assertThatThrownBy {
            service.getMyTeam(GetMyTeamUseCase.Query(gameId, teamId, memberId), tenant)
        }.isInstanceOf(TeamMemberNotFoundException::class.java)
    }

    @Test
    fun `throws when the supplied member belongs to a different game`() {
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns aTeam()
        every { getTeamMemberPort.getTeamMemberOrNull(memberId, tenant) } returns aMember(game = GameId("other"))

        assertThatThrownBy {
            service.getMyTeam(GetMyTeamUseCase.Query(gameId, teamId, memberId), tenant)
        }.isInstanceOf(TeamMemberNotFoundException::class.java)
    }
}
