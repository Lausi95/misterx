package net.lausi95.citygame.adapter.out.persistence.game

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import net.lausi95.citygame.application.domain.model.game.Game

@Entity
@Table(name = "map")
class MapEntity() {

    @Id
    @Column(name = "id")
    var id: String? = null

    @Column(name = "game_id")
    var gameId: String? = null

    @Column(name = "corner_a_latitude")
    var cornerALatitude: Double? = null

    @Column(name = "corner_a_longitude")
    var cornerALongitude: Double? = null

    @Column(name = "corner_b_latitude")
    var cornerBLatitude: Double? = null

    @Column(name = "corner_b_longitude")
    var cornerBLongitude: Double? = null

    @Column(name = "rows")
    var rows: Int? = null

    @Column(name = "columns")
    var columns: Int? = null

    constructor(game: Game) : this() {
        this.id = game.map.id.value
        this.gameId = game.id.value
        this.cornerALatitude = game.map.cornerA.latitude
        this.cornerALongitude = game.map.cornerA.longitude
        this.cornerBLatitude = game.map.cornerB.latitude
        this.cornerBLongitude = game.map.cornerB.longitude
        this.rows = game.map.grid.rows
        this.columns = game.map.grid.columns
    }
}
