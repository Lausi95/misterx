package net.lausi95.citygame.adapter.`in`.web.controller.team

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.application.port.`in`.finding.FindingUseCase
import net.lausi95.citygame.application.port.`in`.team.TeamUseCase
import net.lausi95.citygame.common.Tenant
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "My Team")
@RestController
@RequestMapping("/my-team")
class MyTeamController(
    private val teamUseCase: TeamUseCase,
    private val findingUseCase: FindingUseCase,
) {

    @Operation(summary = "Returns the team the caller belongs to, identified by request headers")
    @ApiResponse(
        responseCode = "200",
        description = "The caller's team",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = TeamResource::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Team not found in this game, or the supplied member is not a member of this team",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping
    fun getMyTeam(
        @RequestHeader("X-GameId") gameId: String,
        @RequestHeader("X-TeamId") teamId: String,
        @RequestHeader(value = "X-MemberId", required = false) memberId: String?,
        tenant: Tenant,
    ): TeamResource {
        val query = TeamUseCase.GetMyTeamQuery(
            GameId(gameId),
            TeamId(teamId),
            memberId?.let { TeamMemberId(it) },
        )

        val team = teamUseCase.getMyTeam(query, tenant)
        val memberCount = teamUseCase.countTeamMembers(team.id, tenant)
        val foundAgents = findingUseCase.getFoundAgents(team.id, tenant)

        return TeamResource(team, memberCount, foundAgents)
    }
}
