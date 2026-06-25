package net.lausi95.citygame.adapter.persistence.team

import net.lausi95.citygame.DatabaseIntegrationTest
import net.lausi95.citygame.adapter.out.persistence.game.GameEntity
import net.lausi95.citygame.adapter.out.persistence.game.GameEntityJpaRepository
import net.lausi95.citygame.adapter.out.persistence.team.TeamEntity
import net.lausi95.citygame.adapter.out.persistence.team.TeamEntityRepository
import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.game.MapId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime

@DatabaseIntegrationTest
class TeamEntityRepositoryTest {

    @Autowired
    private lateinit var gameEntityJpaRepository: GameEntityJpaRepository

    @Autowired
    private lateinit var teamEntityRepository: TeamEntityRepository

    private val tenant = Tenant("https://acme.city-game.net")

    private fun seedGame(): GameId {
        val game = Game(
            GameId(),
            GameTitle("Hunt"),
            OffsetDateTime.now().minusHours(1),
            OffsetDateTime.now().plusHours(1),
            Map(MapId(), GeoLocation(0.0, 0.0), GeoLocation(1.0, 1.0), Grid(10, 10)),
        )
        gameEntityJpaRepository.saveAndFlush(GameEntity(game, tenant))
        return game.id
    }

    private fun seedTeam(gameId: GameId, name: String) {
        teamEntityRepository.saveAndFlush(TeamEntity(Team(TeamId(), gameId, name), tenant))
    }

    @Test
    fun `orders teams by name German-correct and case-insensitive`() {
        val gameId = seedGame()
        // Insertion order deliberately scrambled. Under Postgres' default byte collation this
        // would sort to [Apfel, apple, zebra, Ärger]; the German ICU collation must instead
        // fold case (Apfel before apple) and pull the umlaut next to its base letter (Ärger
        // between the a-words and zebra). See ADR 0015.
        seedTeam(gameId, "zebra")
        seedTeam(gameId, "Ärger")
        seedTeam(gameId, "Apfel")
        seedTeam(gameId, "apple")

        val ordered = teamEntityRepository
            .findByGameIdAndTenant(gameId.value, tenant.value, PageRequest.of(0, 10, Sort.by("name")))
            .map { it.toTeam().name }
            .content

        assertThat(ordered).containsExactly("Apfel", "apple", "Ärger", "zebra")
    }

    @Test
    fun `only returns teams of the given game and tenant`() {
        val gameId = seedGame()
        val otherGameId = seedGame()
        val otherTenant = Tenant("https://other.city-game.net")

        seedTeam(gameId, "Red")
        seedTeam(otherGameId, "Blue")
        teamEntityRepository.saveAndFlush(TeamEntity(Team(TeamId(), gameId, "Green"), otherTenant))

        val names = teamEntityRepository
            .findByGameIdAndTenant(gameId.value, tenant.value, PageRequest.of(0, 10, Sort.by("name")))
            .map { it.toTeam().name }
            .content

        assertThat(names).containsExactly("Red")
    }
}
