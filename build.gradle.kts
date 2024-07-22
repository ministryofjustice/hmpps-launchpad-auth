plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.1"
  kotlin("plugin.spring") version "2.0.0"
  kotlin("plugin.jpa") version "2.0.0"
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
  implementation("org.springframework.security:spring-security-crypto:6.2.4")
  implementation("io.jsonwebtoken:jjwt-api:0.11.5")
  implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
  implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
  implementation("org.json:json:20231013")
  implementation("org.flywaydb:flyway-core:10.14.0")
  implementation("org.postgresql:postgresql:42.7.3")
  implementation("org.ehcache:ehcache:3.10.8")
  implementation("javax.cache:cache-api:1.1.1")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("com.h2database:h2:2.0.204")
  testImplementation("org.wiremock:wiremock-standalone:3.3.1")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
}
