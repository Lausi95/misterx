package net.lausi95.citygame.application.usecase.game.updategame

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import net.lausi95.citygame.application.port.`in`.game.UpdateGameUseCase
import net.lausi95.citygame.application.port.`in`.game.updategame.UpdateGameCommand
import net.lausi95.citygame.bdd.random
import net.lausi95.citygame.domain.Tenant
import net.lausi95.citygame.domain.game.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UpdateGameUseCaseTest {

    @MockK
    private lateinit var gameRepository: GameRepository
    private lateinit var updateGameUseCase: UpdateGameUseCase

    @BeforeEach
    fun setUp() {
        updateGameUseCase = UpdateGameUseCase(gameRepository)
    }

    @Test
    fun `cannot update a game that does not exist`() {
        val tenant = Tenant.random()
        val gameId = GameId.random()

        every { gameRepository.findById(gameId, tenant) } returns null
        every { gameRepository.existsByTitle(any(), tenant) } returns false

        val exception = assertThrows<GameNotFoundException> {
            val command = UpdateGameCommand(
                gameId = gameId,
                title = GameTitle.random()
            )
            updateGameUseCase(command, tenant)
        }

        assertThat(exception.message).isEqualTo("Game not found: $gameId")
    }

    @Test
    fun `cannot update game when title already exists`() {
        val tenant = Tenant.random()
        val gameTitle = GameTitle.random()
        val gameId = GameId.random()

        every { gameRepository.existsByTitle(gameTitle, tenant) } returns true

        val exception = assertThrows<GameTitleAlreadyExistsException> {
            val command = UpdateGameCommand(
                gameId = gameId,
                title = gameTitle
            )
            updateGameUseCase(command, tenant)
        }

        assertThat(exception.message).isEqualTo("Game title already exist: $gameTitle")
    }
}
