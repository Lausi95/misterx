package net.lausi95.citygame.adapter.`in`.web.controller.team

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.`in`.team.GetTeamMembersUseCase
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.*

@Tag(name = "Team Members")
@RestController
@RequestMapping("/games/{gameId}/teams/{teamId}/members")
class TeamMemberController(
    private val getTeamMembersUseCase: GetTeamMembersUseCase,
) {

    @Operation(summary = "Returns a paginated collection of members for a team")
    @ApiResponse(
        responseCode = "200",
        description = "Collection of team members",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = TeamMemberCollection::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping
    fun getTeamMembers(
        @PageableDefault pageable: Pageable,
        @PathVariable gameId: String,
        @PathVariable teamId: String,
        tenant: Tenant,
    ): TeamMemberCollection {
        val members = getTeamMembersUseCase.getTeamMembers(TeamId(teamId), GameId(gameId), pageable, tenant)
        return TeamMemberCollection(members)
    }
}
