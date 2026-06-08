package net.lausi95.citygame.application.usecase.game.creategame

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import net.lausi95.citygame.application.port.`in`.game.CreateGameUseCase
import net.lausi95.citygame.application.port.`in`.game.creategame.CreateGameCommand
import net.lausi95.citygame.bdd.random
import net.lausi95.citygame.domain.Tenant
import net.lausi95.citygame.domain.game.Game
import net.lausi95.citygame.domain.game.GameRepository
import net.lausi95.citygame.domain.game.GameTitle
import net.lausi95.citygame.domain.game.GameTitleAlreadyExistsException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CreateGameUseCaseTest {

    @MockK(relaxed = true)
    private lateinit var gameRepository: GameRepository
    private lateinit var createGameUseCase: CreateGameUseCase

    @BeforeEach
    fun setUp() {
        createGameUseCase = CreateGameUseCase(gameRepository)
    }

    @Test
    fun `should create game when conditions meet`() {
        val tenant = Tenant.random()
        val gameTitle = GameTitle.random()
        every { gameRepository.existsByTitle(any(), tenant) }.answers { false }
        val response = createGameUseCase(CreateGameCommand(gameTitle), tenant)

        val game = slot<Game>()
        verify { gameRepository.save(capture(game), tenant) }

        assertSoftly {
            it.assertThat(game.captured.id).isEqualTo(response.gameId)
            it.assertThat(game.captured.title).isEqualTo(gameTitle)
        }
    }

    @Test
    fun `should throw GameTitleAlreadyExistsException, when game with title already exist`() {
        val tenant = Tenant.random()
        val gameTitle = GameTitle.random()
        every { gameRepository.existsByTitle(any(), tenant) }.answers { true }
        val exception = assertThrows<GameTitleAlreadyExistsException> {
            createGameUseCase(CreateGameCommand(gameTitle), tenant)
        }

        verify(exactly = 0) { gameRepository.save(any(), tenant) }
        assertThat(exception.message).isEqualTo("Game title already exist: ${gameTitle.value}")
    }
}