package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.finding.FoundAgent
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import org.springframework.data.domain.Page

@Schema(description = "Paginated collection of teams")
data class TeamCollection(

    @Schema(description = "Page of team resources")
    @JsonUnwrapped
    val teams: Page<TeamResource>,

    @Schema(description = "Navigation links")
    val links: Map<String, String>,
) {
    constructor(teams: Page<Team>, countFn: (Team) -> Long, foundAgentsByTeam: Map<TeamId, List<FoundAgent>>) : this(
        teams = teams.map { TeamResource(it, countFn(it), foundAgentsByTeam.getOrDefault(it.id, emptyList())) },
        links = mapOf(),
    )
}
