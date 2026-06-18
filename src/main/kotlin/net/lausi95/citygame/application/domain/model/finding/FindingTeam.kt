package net.lausi95.citygame.application.domain.model.finding

import net.lausi95.citygame.application.domain.model.team.TeamId

/**
 * Projection of a team that has found an agent, exposing only the team's identity and display name.
 */
class FindingTeam(
    val teamId: TeamId,
    val name: String,
)
