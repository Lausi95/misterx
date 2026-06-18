package net.lausi95.citygame.adapter.out.persistence.team

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

internal interface TeamMemberEntityRepository : JpaRepository<TeamMemberEntity, String> {

    fun findByTeamIdAndGameIdAndTenant(teamId: String, gameId: String, tenant: String, pageable: Pageable): Page<TeamMemberEntity>

    fun findByIdAndTenant(id: String, tenant: String): TeamMemberEntity?

    fun countByTeamIdAndTenant(teamId: String, tenant: String): Long
}
