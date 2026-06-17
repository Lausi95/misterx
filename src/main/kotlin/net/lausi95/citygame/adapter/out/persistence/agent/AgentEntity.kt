package net.lausi95.citygame.adapter.out.persistence.agent

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

@Entity
@Table(name = "agent")
internal class AgentEntity {

    @Id
    @Column(name = "id")
    var id: String? = null

    @Column(name = "tenant")
    var tenant: String? = null

    @Column(name = "game_id")
    var gameId: String? = null

    @Column(name = "type")
    var type: String? = null

    @Column(name = "phone_number")
    var phoneNumber: String? = null

    @Column(name = "first_name")
    var firstName: String? = null

    @Column(name = "last_name")
    var lastName: String? = null

    @Column(name = "alias")
    var alias: String? = null

    @Column(name = "active")
    var active: Boolean? = null

    constructor(agent: Agent, tenant: Tenant) {
        this.id = agent.id.value
        this.tenant = tenant.value
        this.gameId = agent.gameId.value
        this.type = agent.type.name
        this.phoneNumber = agent.phoneNumber
        this.firstName = agent.firstname
        this.lastName = agent.lastName
        this.alias = agent.alias
        this.active = agent.active
    }

    fun toAgent() = Agent(
        AgentId(id!!),
        GameId(gameId!!),
        Agent.Type.valueOf(type!!),
        phoneNumber!!,
        firstName!!,
        lastName!!,
        alias!!,
        active!!
    )
}
