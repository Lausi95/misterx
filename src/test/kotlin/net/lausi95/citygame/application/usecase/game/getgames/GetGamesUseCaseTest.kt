package net.lausi95.citygame.application.usecase.game.getgames

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import net.lausi95.citygame.application.port.`in`.game.GetGamesUseCase
import net.lausi95.citygame.bdd.random
import net.lausi95.citygame.domain.Tenant
import net.lausi95.citygame.domain.game.Game
import net.lausi95.citygame.domain.game.GameRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@ExtendWith(MockKExtension::class)
class GetGamesUseCaseTest {

    @MockK
    private lateinit var gameRepository: GameRepository
    private lateinit var getGamesUseCase: GetGamesUseCase

    @BeforeEach
    fun setUp() {
        getGamesUseCase = GetGamesUseCase(gameRepository)
    }

    @Test
    fun `should pass down request to game repository and replies with its reply`() {
        val someTenant = Tenant.random()
        val pageable = mockk<Pageable>()
        val page = mockk<Page<Game>>()
        every { gameRepository.find(pageable, someTenant) } returns page

        val result = getGamesUseCase(pageable, someTenant)

        verify { gameRepository.find(pageable, someTenant) }
        assertThat(result).isEqualTo(page)
    }
}
