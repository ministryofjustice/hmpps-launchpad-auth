package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.health

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.health.HealthPingCheck

/**
 * Adds version data to the /health endpoint. This is called by the UI to display API details
 */
@Component
class HealthInfo(buildProperties: BuildProperties) : HealthIndicator {
  private val version: String = buildProperties.version

  override fun health(): Health = Health.up().withDetail("version", version).build()
}

@Component("hmppsAuth")
class AuthHealthInfo(@Qualifier("hmppsAuthHealthWebClient") webClient: WebClient, buildProperties: BuildProperties) : HealthPingCheck(webClient)

@Component("prisonerApi")
class PrisonApiHealthInfo(
  @Qualifier("prisonApiHealthWebClient") webClient: WebClient,
  buildProperties: BuildProperties,
  private val telemetryClient: TelemetryClient,
) : HealthPingCheck(webClient) {

  override fun health(): Health {
    val health = super.health()
    if (health.status.code != "UP") {
      telemetryClient.trackException(
        Exception("PrisonApi health check failed"),
        mapOf("component" to "prisonerApi", "status" to health.status.code),
        null,
      )
    }
    return health
  }
}
