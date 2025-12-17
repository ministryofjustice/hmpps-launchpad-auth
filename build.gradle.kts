plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.2.0"
  kotlin("plugin.spring") version "2.3.0"
  kotlin("plugin.jpa") version "2.3.0"
  id("org.owasp.dependencycheck") version "12.1.9"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("io.jsonwebtoken:jjwt-api:0.13.0")
  implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
  implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")
  implementation("org.json:json:20250517")
  implementation("org.flywaydb:flyway-core:11.19.0")
  implementation("org.postgresql:postgresql:42.7.8")
  implementation("org.ehcache:ehcache:3.11.1")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14") {
    exclude(group = "org.yaml", module = "snakeyaml")
  }

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.8.2")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("com.fasterxml.uuid:java-uuid-generator:5.2.0")
  implementation("com.nimbusds:nimbus-jose-jwt:10.6")

  runtimeOnly("org.flywaydb:flyway-database-postgresql:11.19.0")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.8.2")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.36") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("org.testcontainers:postgresql:1.21.4")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
}

kotlin {
  jvmToolchain(25)
}

java {
  sourceCompatibility = JavaVersion.VERSION_24
  targetCompatibility = JavaVersion.VERSION_24
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24
  }
}

dependencyCheck {
  nvd.datafeedUrl = "file:///opt/vulnz/cache"
}
