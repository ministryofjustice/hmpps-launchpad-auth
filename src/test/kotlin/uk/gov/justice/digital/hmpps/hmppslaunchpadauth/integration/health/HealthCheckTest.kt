package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.integration.health

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.integration.IntegrationTestBase

class HealthCheckTest : IntegrationTestBase() {

  @Test
  fun `DB Health page reports ok`() {
    webTestClient.get()
      .uri("/health/db")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  fun `Auth Health page reports ok`() {
    webTestClient.get()
      .uri("/health/hmppsAuth")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health info reports version`() {
    webTestClient.get()
      .uri("/health/healthInfo")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `Health ping page is accessible`() {
    webTestClient.get()
      .uri("/health/ping")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `readiness reports ok`() {
    webTestClient.get()
      .uri("/health/readiness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }

  @Test
  fun `liveness reports ok`() {
    webTestClient.get()
      .uri("/health/liveness")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("status").isEqualTo("UP")
  }
}
