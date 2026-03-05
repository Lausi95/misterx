package net.lausi95.citygame.integration

import net.lausi95.citygame.IntegrationTest
import net.lausi95.citygame.Keycloak
import net.lausi95.citygame.bdd.random
import net.lausi95.citygame.domain.Tenant
import net.lausi95.citygame.domain.game.GameId
import net.lausi95.citygame.domain.game.GameTitle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient

@IntegrationTest
class GameIntegrationTest {

    @Autowired
    private lateinit var restTestClient: RestTestClient

    @Autowired
    private lateinit var keycloak: Keycloak

    @Test
    fun `can create and fetch game`() {
        val tenant = Tenant.random()
        val gameTitle = GameTitle.random()

        val gameLocation = createGame(gameTitle, tenant)
        val gameResponse = getGame(gameLocation, tenant)

        gameResponse.jsonPath("$.title").value<String> {
            assertThat(it).isEqualTo(gameTitle.value)
        }
    }

    @Test
    fun `create multiple games and fetch list`() {
        val tenant = Tenant.random()
        val gameAmount = 5
        val gameTitles = (1..gameAmount).map { GameTitle.random() }

        gameTitles.forEach {
            createGame(it, tenant)
        }
        val gamesResponses = getGames(tenant)

        gamesResponses.jsonPath("$.content.size()").value<Int> {
            assertThat(it).isEqualTo(gameAmount)
        }

        val indices = (0..10).toList()
        assertThat(gameTitles).allSatisfy { title ->
            assertThat(indices).anySatisfy {
                gamesResponses.jsonPath("$.content[$it].title").value<String> { json ->
                    assertThat(json).isEqualTo(title.value)
                }
            }
        }
    }

    @Test
    fun `create multiple games and check pagination`() {
        val tenant = Tenant.random()
        val gameAmount = 15
        val gameTitles = (1..gameAmount).map { GameTitle.random() }

        gameTitles.forEach {
            createGame(it, tenant)
        }
        val gamesResponsePage1 = getGames(tenant, page = 0)
        val gamesResponsePage2 = getGames(tenant, page = 1)

        gamesResponsePage1.jsonPath("$.content.size()").value<Int> {
            assertThat(it).isEqualTo(10)
        }
        gamesResponsePage2.jsonPath("$.content.size()").value<Int> {
            assertThat(it).isEqualTo(5)
        }
    }

    @Test
    fun `different tenants can create games with the same title`() {
        val tenant1 = Tenant.random()
        val tenant2 = Tenant.random()
        val gameTitle = GameTitle.random()

        assertDoesNotThrow {
            createGame(gameTitle, tenant1)
            createGame(gameTitle, tenant2)
        }
    }

    @Test
    fun `change game title`() {
        val tenant = Tenant.random()
        val oldGameTile = GameTitle.random()
        val newGameTitle = GameTitle.random()

        val location = createGame(oldGameTile, tenant)
        val gameId = GameId.fromLocation(location)

        changeGameTitle(gameId, newGameTitle, tenant)

        val game = getGame(gameId, tenant)
        game.jsonPath("$.title").value<String> {
            assertThat(it).isEqualTo(newGameTitle.value)
        }
    }

    private fun getGame(gameLocation: String, tenant: Tenant): RestTestClient.BodyContentSpec {
        return restTestClient.get()
            .uri(gameLocation)
            .header("Authorization", "Bearer ${keycloak.getJwtToken()}")
            .header(Tenant.OVERRIDE_TENANT_HEADER_NAME, tenant.value)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
    }

    private fun getGame(gameId: GameId, tenant: Tenant): RestTestClient.BodyContentSpec {
        return restTestClient.get()
            .uri("/games/{gameId}", gameId.value)
            .header("Authorization", "Bearer ${keycloak.getJwtToken()}")
            .header(Tenant.OVERRIDE_TENANT_HEADER_NAME, tenant.value)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
    }

    private fun createGame(title: GameTitle, tenant: Tenant): String {
        val result = restTestClient.post()
            .uri("/games")
            .header("Authorization", "Bearer ${keycloak.getJwtToken()}")
            .header(Tenant.OVERRIDE_TENANT_HEADER_NAME, tenant.value)
            .contentType(MediaType.APPLICATION_JSON)
            .body( // language=json
                """
                {
                  "title": "${title.value}"
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isCreated()
            .returnResult()

        return result.responseHeaders.location.toString()
    }

    private fun changeGameTitle(gameId: GameId, newTitle: GameTitle, tenant: Tenant) {
        restTestClient.put()
            .uri("/games/{gameId}", gameId.value)
            .header("Authorization", "Bearer ${keycloak.getJwtToken()}")
            .header(Tenant.OVERRIDE_TENANT_HEADER_NAME, tenant.value)
            .contentType(MediaType.APPLICATION_JSON)
            .body( // language=json
                """
                {
                  "title": "${newTitle.value}"
                }
                """.trimIndent()
            )
            .exchange()
            .expectStatus().isAccepted()
            .returnResult()
    }

    private fun getGames(tenant: Tenant, page: Int = 0): RestTestClient.BodyContentSpec {
        return restTestClient.get()
            .uri("/games?page=$page")
            .header("Authorization", "Bearer ${keycloak.getJwtToken()}")
            .header(Tenant.OVERRIDE_TENANT_HEADER_NAME, tenant.value)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
    }

    fun GameId.Companion.fromLocation(location: String): GameId {
        return GameId(location.split("/").last())
    }
}
