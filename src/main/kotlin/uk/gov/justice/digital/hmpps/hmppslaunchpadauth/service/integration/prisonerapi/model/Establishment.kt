package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import com.fasterxml.jackson.annotation.JsonProperty


data class Establishment(
  @JsonProperty("agency_id")
  val agencyId: String,
  val name: String,
  @JsonProperty("display_name")
  val displayName: String,
  val youth: Boolean
)
