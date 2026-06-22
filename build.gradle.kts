plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.spring") version "2.2.10"
    kotlin("plugin.jpa") version "2.2.10"

    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"

    jacoco
}

group = "de.lausi95"
version = "0.0.1-SNAPSHOT"
description = "city-game-backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

extra["springModulithVersion"] = "2.0.1"
extra["mockkVersion"] = "1.14.7"
extra["springmockkVersion"] = "5.0.1"
extra["qrcodegenVersion"] = "1.8.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.oshai:kotlin-logging:7.0.14")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-junit-jupiter")
        exclude(module = "mockito-core")
    }
    testImplementation("io.mockk:mockk:${property("mockkVersion")}")
    testImplementation("com.ninja-squad:springmockk:${property("springmockkVersion")}")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("com.github.dasniko:testcontainers-keycloak:4.1.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("io.nayuki:qrcodegen:${property("qrcodegenVersion")}")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    jvmArgs("-Xshare:off")
}

tasks.jacocoTestReport {
    reports {
        csv.required.set(false)
    }
}

tasks.bootJar {
    archiveFileName = "application.jar"
}
