package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.health.HealthPingCheck

/**
 * Adds version data to the /health endpoint. This is called by the UI to display API details
 */
@Component
class HealthInfo(buildProperties: BuildProperties) : HealthIndicator {
  private val version: String = buildProperties.version.toString()

  override fun health(): Health = Health.up().withDetail("version", version).build()
}

@Component("hmppsAuth")
class AuthHealthInfo(@Qualifier("hmppsAuthHealthWebClient") webClient: WebClient, buildProperties: BuildProperties) : HealthPingCheck(webClient)

@Component("prisonerApi")
class PrisonApiHealthInfo(@Qualifier("prisonApiHealthWebClient") webClient: WebClient, buildProperties: BuildProperties) : HealthPingCheck(webClient)
