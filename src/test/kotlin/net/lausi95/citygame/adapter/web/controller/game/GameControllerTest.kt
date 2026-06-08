package net.lausi95.citygame.adapter.web.controller.game

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import net.lausi95.citygame.adapter.`in`.web.controller.game.GameController
import net.lausi95.citygame.application.port.`in`.game.CreateGameUseCase
import net.lausi95.citygame.application.port.`in`.game.GetGameUseCase
import net.lausi95.citygame.application.port.`in`.game.GetGamesUseCase
import net.lausi95.citygame.application.port.`in`.game.UpdateGameUseCase
import net.lausi95.citygame.application.port.`in`.game.creategame.CreateGameCommand
import net.lausi95.citygame.application.port.`in`.game.creategame.CreateGameResult
import net.lausi95.citygame.application.port.`in`.game.updategame.UpdateGameCommand
import net.lausi95.citygame.bdd.random
import net.lausi95.citygame.bdd.randomGame
import net.lausi95.citygame.domain.DomainException
import net.lausi95.citygame.domain.Tenant
import net.lausi95.citygame.domain.game.Game
import net.lausi95.citygame.domain.game.GameId
import net.lausi95.citygame.domain.game.GameNotFoundException
import net.lausi95.citygame.domain.game.GameTitle
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@WebMvcTest(GameController::class)
class GameControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean(relaxed = true)
    private lateinit var createGameUseCase: CreateGameUseCase

    @MockkBean(relaxed = true)
    private lateinit var getGameUseCase: GetGameUseCase

    @MockkBean(relaxed = true)
    private lateinit var getGamesUseCase: GetGamesUseCase

    @MockkBean(relaxed = true)
    private lateinit var updateGameUseCase: UpdateGameUseCase

    @Nested
    @DisplayName("POST /games")
    inner class PostGame {
        @Test
        fun `should map pass request to use case and responds with 201 and location header with id from use case`() {
            val createGameResult = CreateGameResult(
                gameId = GameId("some-game-id")
            )
            every { createGameUseCase(any(), any()) }.answers { createGameResult }

            mockMvc.post("/games") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt())
                content = /* language=json */ """
                {
                    "title": "City-Game Luckenwalde 2026"
                }
            """.trimIndent()
            }.andExpect {
                status { isCreated() }
                header {
                    stringValues("location", "http://localhost/games/some-game-id")
                }
            }

            val createGameCommand = slot<CreateGameCommand>()
            verify { createGameUseCase(capture(createGameCommand), any()) }

            assertThat(createGameCommand.captured.title).isEqualTo(GameTitle("City-Game Luckenwalde 2026"))
        }

        @Test
        fun `should respond with bad request, when title is not present`() {
            mockMvc.post("/games") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt())
                content = /* language=json */ """
                {
                }
            """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.details", equalTo("Validation Error"))
                jsonPath("$.title", equalTo("'title' cannot be null or empty"))
            }
        }

        @Test
        fun `should respond with bad request, when title is empty string`() {
            mockMvc.post("/games") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt())
                content = /* language=json */ """
                {
                    "title": ""
                }
            """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.details", equalTo("Validation Error"))
                jsonPath("$.title", equalTo("'title' cannot be null or empty"))
            }
        }

        @Test
        fun `should response with bad request, when request is no valid json`() {
            mockMvc.post("/games") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt())
                content = """
                    "title": ""
                }
            """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.details", equalTo("Input Error: Invalid JSON"))
            }
        }

        @Test
        fun `should respond with bad request, when use case throws an illegal argument exception`() {
            every { createGameUseCase(any(), any()) }.throws(DomainException("Something went wrong"))
            mockMvc.post("/games") {
                contentType = MediaType.APPLICATION_JSON
                with(jwt())
                content = """
                {
                    "title": "hallo"
                }
            """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.details", equalTo("Something went wrong"))
            }
        }
    }

    @Nested
    @DisplayName("GET /games/{gameId}")
    inner class GetGame {
        @Test
        fun `should response with not found, when game with given id does not exist`() {
            val gameId = GameId.random()
            every { getGameUseCase(gameId, any()) }.throws(GameNotFoundException("Game not found"))
            mockMvc.get("/games/{gameId}", gameId.value) {
                with(jwt())
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.details", equalTo("Game not found"))
            }
        }

        @Test
        fun `should response with game resource, when the requested game does exist`() {
            val game = Game(GameId("some-game-id"), GameTitle("some game title"))
            every { getGameUseCase(game.id, any()) }.answers { game }
            mockMvc.get("/games/{gameId}", game.id) {
                with(jwt())
            }.andExpect {
                status { isOk() }
                jsonPath("$.id", equalTo("some-game-id"))
                jsonPath("$.title", equalTo("some game title"))
                jsonPath("$.links.self", equalTo("/games/some-game-id"))
            }
        }
    }

    @Nested
    @DisplayName("GET /games")
    inner class GetGames {
        @Test
        fun `should respond with first page of games`() {
            // arrange
            val games = (1..10).map { randomGame() }
            every { getGamesUseCase(any(), any()) } answers { PageImpl(games) }

            // act
            mockMvc.get("/games") {
                with(jwt())
            }.andExpect {
                // assert
                status { isOk() }
                jsonPath("$.content.length()", equalTo(10))
                jsonPath("$.size", equalTo(10))
                jsonPath("$.number", equalTo(0))
                jsonPath("$.totalPages", equalTo(1))
                jsonPath("$.links.self", equalTo("/games"))
            }
        }

        @Test
        fun `should pass default params to pagable`() {
            // arrange
            every { getGamesUseCase(any(), any()) } answers { PageImpl(emptyList()) }

            // act
            mockMvc.get("/games") {
                with(jwt())
            }.andExpect { status { isOk() } }

            // assert
            val pageable = slot<Pageable>()
            val tenant = slot<Tenant>()
            verify { getGamesUseCase(capture(pageable), capture(tenant)) }

            assertThat(pageable.captured.pageSize).isEqualTo(10)
            assertThat(pageable.captured.pageNumber).isEqualTo(0)
            assertThat(tenant.captured).isEqualTo(Tenant("localhost"))
        }

        @Test
        fun `should pass page size to pageable`() {
            // arrange
            every { getGamesUseCase(any(), any()) } answers { PageImpl(emptyList()) }

            // act
            mockMvc.get("/games") {
                param("size", "20")
                with(jwt())
            }.andExpect { status { isOk() } }

            // assert
            val pageable = slot<Pageable>()
            verify { getGamesUseCase(capture(pageable), any()) }

            assertThat(pageable.captured.pageSize).isEqualTo(20)
        }

        @Test
        fun `should pass page number to pageable`() {
            // arrange
            every { getGamesUseCase(any(), any()) } answers { PageImpl(emptyList()) }

            // act
            mockMvc.get("/games") {
                param("page", "3")
                with(jwt())
            }.andExpect { status { isOk() } }

            // assert
            val pageable = slot<Pageable>()
            verify { getGamesUseCase(capture(pageable), any()) }

            assertThat(pageable.captured.pageNumber).isEqualTo(3)
        }
    }

    @Nested
    @DisplayName("PUT /games/{gameId}")
    inner class UpdateGame {

        @Test
        fun `should accept valid request`() {
            val gameId = GameId.random()
            val gameTitle = GameTitle.random()

            mockMvc.put("/games/{gameId}", gameId.value) {
                contentType = MediaType.APPLICATION_JSON
                with(jwt())
                content = updateGameRequest(gameTitle)
            }.andExpect { status { isAccepted() } }
        }

        @Test
        fun `should deny request with empty title`() {
            val gameId = GameId.random()
            val gameTitle = GameTitle("")

            mockMvc.put("/games/{gameId}", gameId.value) {
                contentType = MediaType.APPLICATION_JSON
                with(jwt())
                content = updateGameRequest(gameTitle)
            }.andExpect { status { isBadRequest() } }
        }

        @Test
        fun `should map request to command and pass to use case`() {
            val gameId = GameId.random()
            val gameTitle = GameTitle.random()

            mockMvc.put("/games/{gameId}", gameId.value) {
                contentType = MediaType.APPLICATION_JSON
                with(jwt())
                content = updateGameRequest(gameTitle)
            }.andExpect { status { isAccepted() } }

            val updateGameCommandSlot = slot<UpdateGameCommand>()
            val tenant = slot<Tenant>()
            verify { updateGameUseCase(capture(updateGameCommandSlot), capture(tenant)) }

            val command = updateGameCommandSlot.captured
            assertThat(command.gameId).isEqualTo(gameId)
            assertThat(command.title).isEqualTo(gameTitle)
            assertThat(tenant.captured).isNotNull()
        }

        fun updateGameRequest(title: GameTitle) = /* language=json */ """
            {
                "title": "${title.value}"
            }
        """.trimIndent()
    }
}
