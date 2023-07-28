plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.2.0"
  kotlin("plugin.spring") version "1.8.21"
  kotlin("plugin.jpa") version "1.8.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("com.microsoft.azure:azure-storage:8.6.2")
  implementation("com.auth0:java-jwt:3.16.0")
  implementation("io.jsonwebtoken:jjwt:0.9.1")
  implementation("org.json:json:20230618")
  implementation("org.flywaydb:flyway-core:9.19.4")
  implementation("org.postgresql:postgresql:42.6.0")
  implementation("javax.xml.bind:jaxb-api:2.3.0")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("com.h2database:h2:2.0.204")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(19))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "19"
    }
  }
}
