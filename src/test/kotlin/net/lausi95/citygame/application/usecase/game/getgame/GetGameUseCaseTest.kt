package net.lausi95.citygame.application.usecase.game.getgame

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import net.lausi95.citygame.application.port.`in`.game.GetGameUseCase
import net.lausi95.citygame.bdd.random
import net.lausi95.citygame.domain.Tenant
import net.lausi95.citygame.domain.game.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetGameUseCaseTest {

    @MockK
    private lateinit var gameRepository: GameRepository
    private lateinit var getGameUseCase: GetGameUseCase

    @BeforeEach
    fun setUp() {
        getGameUseCase = GetGameUseCase(gameRepository)
    }

    @Test
    fun `should return game when game with given id exists`() {
        val tenant = Tenant.random()
        val game = Game(GameId("some-game-id"), GameTitle("some-game-title"))
        every { gameRepository.findById(GameId("some-game-id"), tenant) }.answers { game }

        val result = getGameUseCase(GameId("some-game-id"), tenant)

        assertThat(result).isEqualTo(game)
    }

    @Test
    fun `should throw GameNotFoundException, when game with given id does not exist`() {
        val someTenant = Tenant.random()
        val someGameId = GameId.random()
        every { gameRepository.findById(someGameId, someTenant) }.answers { null }

        val exception = assertThrows<GameNotFoundException> {
            getGameUseCase(someGameId, someTenant)
        }

        assertThat(exception.message).isEqualTo("Game not found: ${someGameId.value}")
    }
}
