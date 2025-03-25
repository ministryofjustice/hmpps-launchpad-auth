plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.0.0"
  kotlin("plugin.spring") version "2.1.10"
  kotlin("plugin.jpa") version "2.1.10"
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
  implementation("io.jsonwebtoken:jjwt-api:0.11.5")
  implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
  implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
  implementation("org.json:json:20231013")
  implementation("org.flywaydb:flyway-core:10.16.0")
  implementation("org.postgresql:postgresql:42.7.3")
  implementation("org.ehcache:ehcache:3.10.8")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0") {
    exclude(group = "org.yaml", module = "snakeyaml")
  }

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.7")

  runtimeOnly("org.flywaydb:flyway-database-postgresql:10.16.0")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("com.h2database:h2:2.3.232")
  testImplementation("org.wiremock:wiremock-standalone:3.3.1")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
