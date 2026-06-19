package net.lausi95.citygame.application.domain.model.finding

import net.lausi95.citygame.application.domain.model.team.TeamId
import java.time.ZonedDateTime

/**
 * Projection of a team that has found an agent, exposing the team's identity and display name
 * together with when it found the agent.
 */
class FindingTeam(
    val teamId: TeamId,
    val name: String,
    val foundAt: ZonedDateTime,
)
