package net.lausi95.citygame.adapter.out.persistence.agentlocation

import org.springframework.data.jpa.repository.JpaRepository

internal interface AgentLocationEntityRepository : JpaRepository<AgentLocationEntity, String>