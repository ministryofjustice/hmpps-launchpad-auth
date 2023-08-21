package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiError(
  private val error: String,

  @JsonProperty("error_description")
  private val errorDescription: String
)
