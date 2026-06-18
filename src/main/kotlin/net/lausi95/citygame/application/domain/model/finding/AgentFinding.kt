package net.lausi95.citygame.application.domain.model.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.GeoLocation
import java.time.ZonedDateTime
import java.util.*

@JvmInline
value class FindingId(val value: String = UUID.randomUUID().toString()) {
    override fun toString(): String = value
}

class AgentFinding(
    val id: FindingId,
    val gameId: GameId,
    val teamId: TeamId,
    val agentId: AgentId,
    val foundAt: ZonedDateTime,
    val reportedLocation: GeoLocation?,
    val agentLocation: GeoLocation?,
)
