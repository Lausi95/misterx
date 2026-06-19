package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.teamMemberNotFound
import net.lausi95.citygame.application.domain.model.team.teamNotFound
import net.lausi95.citygame.application.port.`in`.team.GetMyTeamUseCase
import net.lausi95.citygame.application.port.out.team.GetTeamMemberPort
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class GetMyTeamService(
    private val getTeamPort: GetTeamPort,
    private val getTeamMemberPort: GetTeamMemberPort,
) : GetMyTeamUseCase {

    override fun getMyTeam(query: GetMyTeamUseCase.Query, tenant: Tenant): Team {
        log.info { "Fetching my team..." }

        val team = getTeamPort.getTeamOrNull(query.teamId, tenant) ?: teamNotFound(query.teamId)
        if (team.gameId != query.gameId) teamNotFound(query.teamId)

        query.memberId?.let { memberId ->
            val member = getTeamMemberPort.getTeamMemberOrNull(memberId, tenant) ?: teamMemberNotFound(memberId)
            if (member.teamId != query.teamId || member.gameId != query.gameId) teamMemberNotFound(memberId)
        }

        log.info { "My team fetched." }

        return team
    }
}
