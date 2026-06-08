package net.lausi95.citygame.adapter.postgresql.game

import net.lausi95.citygame.DatabaseIntegrationTest
import net.lausi95.citygame.adapter.out.persistence.game.GameEntityRepository
import net.lausi95.citygame.adapter.out.persistence.game.GamePersistenceAdapter
import net.lausi95.citygame.bdd.random
import net.lausi95.citygame.domain.Tenant
import net.lausi95.citygame.domain.game.Game
import net.lausi95.citygame.domain.game.GameId
import net.lausi95.citygame.domain.game.GameRepository
import net.lausi95.citygame.domain.game.GameTitle
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable

@DatabaseIntegrationTest
class GameEntityRepositoryTest {

    @Autowired
    private lateinit var gameEntityRepository: GameEntityRepository

    private lateinit var gameRepository: GameRepository

    @BeforeEach
    fun setUp() {
        gameRepository = GamePersistenceAdapter(gameEntityRepository)
    }

    @Test
    fun `should save and load entity`() {
        val tenant = Tenant.random()
        val game = Game(GameId.random(), GameTitle(":)"))
        gameRepository.save(game, tenant)

        val gameFromDb = gameRepository.findById(game.id, tenant)

        assertThat(gameFromDb).isNotNull()
        assertSoftly {
            it.assertThat(gameFromDb?.id).isEqualTo(game.id)
            it.assertThat(gameFromDb?.title).isEqualTo(game.title)
        }
    }

    @Test
    fun `should return null when entity game with id does not exist`() {
        val someTenant = Tenant.random()
        val someGameId = GameId.random()

        val gameFromDb = gameRepository.findById(someGameId, someTenant)

        assertThat(gameFromDb).isNull()
    }

    @Test
    fun `on existsByTitle(), should return true, when game with that title was saved before`() {
        val tenant = Tenant.random()
        val someTitle = GameTitle("some title")
        gameRepository.save(Game(GameId.random(), someTitle), tenant)

        val existsByTitle = gameRepository.existsByTitle(someTitle, tenant)

        assertThat(existsByTitle).isTrue()
    }

    @Test
    fun `on existsByTitle(), should return false, when asking with a different tenant`() {
        val tenant = Tenant.random()
        val otherTenant = Tenant.random()
        val someTitle = GameTitle("some title")
        gameRepository.save(Game(GameId.random(), someTitle), tenant)

        val existsByTitle = gameRepository.existsByTitle(someTitle, otherTenant)

        assertThat(existsByTitle).isFalse()
    }

    @Test
    fun `on existsByTitle(), should return false, when game with that title was never saved`() {
        val tenant = Tenant.random()
        val someTitle = GameTitle("some title")

        val existsByTitle = gameRepository.existsByTitle(someTitle, tenant)

        assertThat(existsByTitle).isFalse()
    }

    @Test
    fun `should load games according to pageable`() {
        // arrange
        val tenant = Tenant.random()
        repeat(10) {
            val gameId = GameId.random()
            val gameTitle = GameTitle.random()
            gameRepository.save(Game(gameId, gameTitle), tenant)
        }
        val pageable1 = Pageable.ofSize(6).withPage(0)
        val pageable2 = Pageable.ofSize(6).withPage(1)

        // act
        val page1 = gameRepository.find(pageable1, tenant)
        val page2 = gameRepository.find(pageable2, tenant)

        // assert
        assertThat(page1.numberOfElements).isEqualTo(6)
        assertThat(page2.numberOfElements).isEqualTo(4)
    }
}
