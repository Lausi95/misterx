package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.game.gameNotFound
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.application.domain.model.team.teamNotFound
import net.lausi95.citygame.application.port.`in`.team.RegisterTeamMemberUseCase
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.application.port.out.team.SaveTeamMemberPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

private val log = KotlinLogging.logger { }

@Service
class RegisterTeamMemberService(
    private val gameRepository: GameRepository,
    private val teamRepository: TeamRepository,
    private val saveTeamMemberPort: SaveTeamMemberPort,
) : RegisterTeamMemberUseCase {

    @Transactional
    override fun registerTeamMember(command: RegisterTeamMemberUseCase.Command, tenant: Tenant): TeamMemberId {
        log.info { "Registering team member..." }

        if (!gameRepository.exists(command.gameId, tenant)) gameNotFound(command.gameId)
        if (!teamRepository.exists(command.teamId, tenant)) teamNotFound(command.teamId)

        val member = TeamMember(
            TeamMemberId(),
            command.teamId,
            command.gameId,
            OffsetDateTime.now(ZoneOffset.UTC),
        )

        saveTeamMemberPort.saveTeamMember(member, tenant)

        log.info { "Team member registered." }

        return member.id
    }
}
