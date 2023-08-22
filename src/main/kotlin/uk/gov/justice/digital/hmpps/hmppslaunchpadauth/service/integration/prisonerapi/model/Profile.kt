package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Profile(
  val id: String,
  @JsonProperty("given_name")
  val givenName: String,
  @JsonProperty("family_name")
  val familyName: String
)
