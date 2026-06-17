package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonUnwrapped
import net.lausi95.citygame.application.domain.model.team.Team
import org.springframework.data.domain.Page

data class TeamCollection(
    @JsonUnwrapped
    val teams: Page<TeamResource>,
    val links: Map<String, String>,
) {
    constructor(teams: Page<Team>) : this(
        teams = teams.map { TeamResource(it) },
        links = mapOf(),
    )
}
