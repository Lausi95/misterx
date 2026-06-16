package net.lausi95.citygame.application.port.out.agent

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.common.Tenant

interface SaveAgentPort {

    fun saveAgent(agent: Agent, tenant: Tenant)
}