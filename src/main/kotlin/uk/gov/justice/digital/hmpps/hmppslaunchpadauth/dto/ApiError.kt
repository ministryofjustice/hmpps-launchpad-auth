package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiError(
  val error: String,

  @JsonProperty("error_description")
  val errorDescription: String
)
