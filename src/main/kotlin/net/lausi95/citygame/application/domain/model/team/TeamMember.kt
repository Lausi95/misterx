package net.lausi95.citygame.application.domain.model.team

import net.lausi95.citygame.application.domain.model.game.GameId
import java.time.OffsetDateTime
import java.util.*

@JvmInline
value class TeamMemberId(val value: String = UUID.randomUUID().toString()) {
    override fun toString(): String = value
}

class TeamMember(
    val id: TeamMemberId,
    val teamId: TeamId,
    val gameId: GameId,
    val registeredAt: OffsetDateTime,
)
