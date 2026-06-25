package net.lausi95.citygame.adapter.out.persistence.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.finding.agentAlreadyFound
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
internal class AgentFindingPersistenceAdapter(
    private val agentFindingEntityJpaRepository: AgentFindingEntityJpaRepository,
) : FindingRepository {

    override fun save(agentFinding: AgentFinding, tenant: Tenant) {
        try {
            // saveAndFlush so a concurrent duplicate that slipped past the pre-check surfaces here,
            // where it can be translated to the same 409 instead of a 500 at commit time.
            agentFindingEntityJpaRepository.saveAndFlush(AgentFindingEntity(agentFinding, tenant))
        } catch (_: DataIntegrityViolationException) {
            agentAlreadyFound(agentFinding.teamId, agentFinding.agentId)
        }
    }

    override fun existsByTeamAndAgent(teamId: TeamId, agentId: AgentId, tenant: Tenant): Boolean {
        return agentFindingEntityJpaRepository.existsByTeamIdAndAgentIdAndTenant(teamId.value, agentId.value, tenant.value)
    }

    override fun byTeam(teamId: TeamId, tenant: Tenant): List<AgentFinding> {
        return agentFindingEntityJpaRepository.findByTeamIdAndTenantOrderByFoundAtDesc(teamId.value, tenant.value)
            .map { it.toAgentFinding() }
    }

    override fun byTeams(teamIds: Collection<TeamId>, tenant: Tenant): List<AgentFinding> {
        return agentFindingEntityJpaRepository.findByTeamIdInAndTenantOrderByFoundAtDesc(teamIds.map { it.value }, tenant.value)
            .map { it.toAgentFinding() }
    }

    override fun byAgent(agentId: AgentId, tenant: Tenant): List<AgentFinding> {
        return agentFindingEntityJpaRepository.findByAgentIdAndTenantOrderByFoundAtDesc(agentId.value, tenant.value)
            .map { it.toAgentFinding() }
    }

    override fun deleteByTeam(teamId: TeamId, tenant: Tenant) {
        agentFindingEntityJpaRepository.deleteByTeamIdAndTenant(teamId.value, tenant.value)
    }

    override fun deleteByAgent(agentId: AgentId, tenant: Tenant) {
        agentFindingEntityJpaRepository.deleteByAgentIdAndTenant(agentId.value, tenant.value)
    }
}
