package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Error")
data class ApiError(
  @Schema(required = true, description = "Type of error.", example = "invalid_code")
  val error: String,

  @Schema(required = true, description = "Error message, containing description.", example = "The client id is invalid.")
  @JsonProperty("error_description")
  val errorDescription: String,
)
