package net.lausi95.citygame.application.domain.model.team

import net.lausi95.citygame.application.domain.NotFoundDomainException

class TeamMemberNotFoundException(message: String) : NotFoundDomainException(message)

fun teamMemberNotFound(memberId: TeamMemberId): Nothing {
    throw TeamMemberNotFoundException("Team member not found: ${memberId.value}")
}
