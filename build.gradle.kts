plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.1.1"
  kotlin("plugin.spring") version "2.2.20"
  kotlin("plugin.jpa") version "2.2.20"
  id("org.owasp.dependencycheck") version "12.1.6"
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
  implementation("org.flywaydb:flyway-core:11.13.2")
  implementation("org.postgresql:postgresql:42.7.7")
  implementation("org.ehcache:ehcache:3.11.1")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13") {
    exclude(group = "org.yaml", module = "snakeyaml")
  }

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.7.0")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("com.fasterxml.uuid:java-uuid-generator:5.1.1")
  implementation("com.nimbusds:nimbus-jose-jwt:10.5")

  runtimeOnly("org.flywaydb:flyway-database-postgresql:11.13.2")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.7.0")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.34") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("com.h2database:h2:2.4.240")
  testImplementation("org.wiremock:wiremock-standalone:3.13.1")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}

dependencyCheck {
  nvd.datafeedUrl = "file:///opt/vulnz/cache"
}
