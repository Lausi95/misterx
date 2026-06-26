package net.lausi95.citygame

import org.junit.jupiter.api.Tag
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(DbContainersConfig::class)
@Tag("IntegrationTest")
annotation class DatabaseIntegrationTest
