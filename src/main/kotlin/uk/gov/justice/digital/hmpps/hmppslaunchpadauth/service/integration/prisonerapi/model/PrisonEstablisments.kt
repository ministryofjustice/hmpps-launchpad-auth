package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "launchpad-auth")
data class PrisonEstablishments(
  var agencies: Map<String, Establishment> = HashMap()
)
