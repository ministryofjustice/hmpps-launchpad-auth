package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import com.fasterxml.jackson.annotation.JsonProperty

data class HmppsAuthAccessToken(
  @JsonProperty("access_token")
  val accessToken: String,
)
