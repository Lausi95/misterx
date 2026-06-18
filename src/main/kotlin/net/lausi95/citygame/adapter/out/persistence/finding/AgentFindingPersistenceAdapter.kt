package net.lausi95.citygame.adapter.out.persistence.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.finding.agentAlreadyFound
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.out.finding.CheckAgentFoundByTeamPort
import net.lausi95.citygame.application.port.out.finding.GetAgentFindingsPort
import net.lausi95.citygame.application.port.out.finding.GetTeamFindingsPort
import net.lausi95.citygame.application.port.out.finding.SaveAgentFindingPort
import net.lausi95.citygame.common.Tenant
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
internal class AgentFindingPersistenceAdapter(
    private val agentFindingEntityRepository: AgentFindingEntityRepository,
) : SaveAgentFindingPort, CheckAgentFoundByTeamPort, GetTeamFindingsPort, GetAgentFindingsPort {

    override fun saveAgentFinding(agentFinding: AgentFinding, tenant: Tenant) {
        try {
            // saveAndFlush so a concurrent duplicate that slipped past the pre-check surfaces here,
            // where it can be translated to the same 409 instead of a 500 at commit time.
            agentFindingEntityRepository.saveAndFlush(AgentFindingEntity(agentFinding, tenant))
        } catch (_: DataIntegrityViolationException) {
            agentAlreadyFound(agentFinding.teamId, agentFinding.agentId)
        }
    }

    override fun teamHasFoundAgent(teamId: TeamId, agentId: AgentId, tenant: Tenant): Boolean {
        return agentFindingEntityRepository.existsByTeamIdAndAgentIdAndTenant(teamId.value, agentId.value, tenant.value)
    }

    override fun getFindingsByTeam(teamId: TeamId, tenant: Tenant): List<AgentFinding> {
        return agentFindingEntityRepository.findByTeamIdAndTenant(teamId.value, tenant.value).map { it.toAgentFinding() }
    }

    override fun getFindingsByAgent(agentId: AgentId, tenant: Tenant): List<AgentFinding> {
        return agentFindingEntityRepository.findByAgentIdAndTenant(agentId.value, tenant.value).map { it.toAgentFinding() }
    }
}
