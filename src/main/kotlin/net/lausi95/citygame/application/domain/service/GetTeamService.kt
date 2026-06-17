package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.`in`.team.GetTeamUseCase
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class GetTeamService(
    private val getTeamPort: GetTeamPort,
) : GetTeamUseCase {

    override fun getTeam(teamId: TeamId, tenant: Tenant): Team {
        log.info { "Fetching team..." }

        val team = getTeamPort.getTeam(teamId, tenant)

        log.info { "Team fetched." }

        return team
    }
}
