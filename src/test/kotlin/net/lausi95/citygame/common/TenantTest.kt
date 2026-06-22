package net.lausi95.citygame.common

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class TenantTest {

    @ParameterizedTest
    @ValueSource(strings = ["https://foo.city-game.net", "http://localhost:3000", "https://bar.city-game.net:8443"])
    fun `accepts a canonical scheme-host-port origin`(origin: String) {
        assertThat(Tenant(origin).value).isEqualTo(origin)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "acme",                              // bare host, no scheme
            "foo.city-game.net",                 // no scheme
            "https://foo.city-game.net/",        // trailing slash (a path)
            "https://foo.city-game.net/find",    // carries a path
            "https://foo.city-game.net?x=1",     // carries a query
            "https://foo.city-game.net#frag",    // carries a fragment
            "https://user@foo.city-game.net",    // carries user-info
            "https://",                          // no host
            "not a uri",                         // malformed
        ]
    )
    fun `rejects anything that is not a canonical origin`(value: String) {
        assertThatThrownBy { Tenant(value) }
            .isInstanceOf(InvalidTenantOriginException::class.java)
    }

    @Test
    fun `fromOrigin rejects a null or blank origin`() {
        assertThatThrownBy { Tenant.fromOrigin(null) }
            .isInstanceOf(InvalidTenantOriginException::class.java)
        assertThatThrownBy { Tenant.fromOrigin("   ") }
            .isInstanceOf(InvalidTenantOriginException::class.java)
    }

    @Test
    fun `fromOrigin builds a tenant from a valid origin`() {
        assertThat(Tenant.fromOrigin("https://foo.city-game.net"))
            .isEqualTo(Tenant("https://foo.city-game.net"))
    }
}
