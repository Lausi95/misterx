package net.lausi95.citygame.adapter.out.persistence.game

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import net.lausi95.citygame.application.domain.model.game.*
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import java.time.OffsetDateTime

@Entity
@Table(name = "game")
class GameEntity() {

    @Id
    @Column(name = "id")
    var id: String? = null

    @Column(name = "tenant")
    var tenant: String? = null

    @Column(name = "title")
    var title: String? = null

    @Column(name = "start_time")
    var startTime: OffsetDateTime? = null

    @Column(name = "end_time")
    var endTime: OffsetDateTime? = null

    constructor(game: Game, tenant: Tenant) : this() {
        this.id = game.id.value
        this.tenant = tenant.value
        this.title = game.title.value
        this.startTime = game.startTime
        this.endTime = game.endTime
    }

    fun toGame(mapEntity: MapEntity) = Game(
        _id = GameId(requireNotNull(id)),
        _title = GameTitle(requireNotNull(title)),
        _startTime = requireNotNull(startTime),
        _endTime = requireNotNull(endTime),
        _map = Map(
            id = MapId(requireNotNull(mapEntity.id)),
            _cornerA = GeoLocation(
                requireNotNull(mapEntity.cornerALatitude),
                requireNotNull(mapEntity.cornerALongitude),
            ),
            _cornerB = GeoLocation(
                requireNotNull(mapEntity.cornerBLatitude),
                requireNotNull(mapEntity.cornerBLongitude),
            ),
            _grid = Grid(
                requireNotNull(mapEntity.rows),
                requireNotNull(mapEntity.columns),
            ),
        )
    )
}
