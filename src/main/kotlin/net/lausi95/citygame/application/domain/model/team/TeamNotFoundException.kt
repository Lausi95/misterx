package net.lausi95.citygame.application.domain.model.team

import net.lausi95.citygame.application.domain.NotFoundDomainException

class TeamNotFoundException(message: String) : NotFoundDomainException(message)

fun teamNotFound(teamId: TeamId): Nothing {
    throw TeamNotFoundException("Team not found: ${teamId.value}")
}
