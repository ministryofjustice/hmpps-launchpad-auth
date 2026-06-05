plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.3.1"
  kotlin("plugin.spring") version "2.4.0"
  kotlin("plugin.jpa") version "2.4.0"
  id("org.owasp.dependencycheck") version "12.2.2"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("io.jsonwebtoken:jjwt-api:0.13.0")
  implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
  implementation("io.jsonwebtoken:jjwt-jackson:0.13.0")
  implementation("org.json:json:20250517")
  implementation("org.postgresql:postgresql:42.7.11")
  implementation("org.ehcache:ehcache:3.12.0")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3") {
    exclude(group = "org.yaml", module = "snakeyaml")
  }

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.5.0")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("com.fasterxml.uuid:java-uuid-generator:5.2.0")
  implementation("com.nimbusds:nimbus-jose-jwt:10.9.1")

  runtimeOnly("org.flywaydb:flyway-database-postgresql:12.8.1")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.5.0")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.43") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("org.testcontainers:postgresql:1.21.4")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.springframework.boot:spring-boot-webtestclient")
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
