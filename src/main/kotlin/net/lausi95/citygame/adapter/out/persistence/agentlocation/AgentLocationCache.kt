package net.lausi95.citygame.adapter.out.persistence.agentlocation

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class AgentLocationCache {

    private val agentLocations: MutableMap<AgentId, AgentLocation> = ConcurrentHashMap()

    fun getAgentLocation(agentId: AgentId): AgentLocation? {
        return agentLocations[agentId]
    }

    fun putAgentLocation(agentId: AgentId, agentLocation: AgentLocation) {
        agentLocations[agentId] = agentLocation
    }

    fun resolve(agentId: AgentId, resolver: (agentId: AgentId) -> AgentLocation?): AgentLocation? {
        var agentLocation = getAgentLocation(agentId)
        if (agentLocation == null) {
            agentLocation = resolver(agentId)
            if (agentLocation != null) {
                putAgentLocation(agentId, agentLocation)
            }
        }
        return agentLocation
    }
}