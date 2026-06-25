package net.lausi95.citygame.application.port.out.agentlocation

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.common.Tenant

/**
 * The persistence seam for the AgentLocation aggregate. One repository per aggregate (ADR 0017),
 * replacing the former single-method ports. [latest] reads the most recent known location for an
 * agent (cache-backed, hence no tenant); [save] and [deleteByAgent] write through to the database.
 */
interface AgentLocationRepository {

    fun save(agentLocation: AgentLocation, tenant: Tenant)

    fun latest(agentId: AgentId): AgentLocation?

    fun deleteByAgent(agentId: AgentId, tenant: Tenant)
}
