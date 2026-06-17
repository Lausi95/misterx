package net.lausi95.citygame.adapter.`in`.web.controller.team

import jakarta.validation.Valid
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.`in`.team.CreateTeamUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamsUseCase
import net.lausi95.citygame.application.port.`in`.team.UpdateTeamUseCase
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/games/{gameId}/teams")
class TeamController(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val createTeamUseCase: CreateTeamUseCase,
    private val getTeamUseCase: GetTeamUseCase,
    private val updateTeamUseCase: UpdateTeamUseCase,
) {

    @GetMapping
    fun getTeams(
        @PageableDefault pageable: Pageable,
        @PathVariable gameId: String,
        @RequestAttribute tenant: Tenant,
    ): TeamCollection {
        val teams = getTeamsUseCase.getTeams(GameId(gameId), pageable, tenant)
        return TeamCollection(teams)
    }

    @PostMapping
    fun createTeam(
        @PathVariable gameId: String,
        @RequestAttribute tenant: Tenant,
        @RequestBody @Valid request: CreateTeamRequest,
    ): ResponseEntity<Unit> {
        val command = CreateTeamUseCase.Command(
            GameId(gameId),
            requireNotNull(request.name),
        )

        val teamId = createTeamUseCase.createTeam(command, tenant)

        val uri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/games/${gameId}/teams/${teamId.value}")
            .build().toUri()

        return ResponseEntity.created(uri).headers {
            it.set("X-TeamId", teamId.value)
        }.build()
    }

    @GetMapping("/{teamId}")
    fun getTeam(
        @PathVariable gameId: String,
        @PathVariable teamId: String,
        @RequestAttribute tenant: Tenant,
    ): TeamResource {
        val team = getTeamUseCase.getTeam(TeamId(teamId), tenant)
        return TeamResource(team)
    }

    @PatchMapping("/{teamId}")
    fun updateTeam(
        @PathVariable gameId: String,
        @PathVariable teamId: String,
        @Valid @RequestBody request: UpdateTeamRequest,
        @RequestAttribute tenant: Tenant,
    ) {
        val command = UpdateTeamUseCase.Command(
            teamId = TeamId(teamId),
            name = request.name,
        )

        updateTeamUseCase.updateTeam(command, tenant)
    }
}
