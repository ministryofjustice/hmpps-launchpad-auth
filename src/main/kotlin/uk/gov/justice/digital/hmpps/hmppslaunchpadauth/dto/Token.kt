package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class Token(
  @Schema(required = true, description = "Id token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
  @JsonProperty("id_token")
  val idToken: String,

  @Schema(required = true, description = "Access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
  @JsonProperty("access_token")
  val accessToken: String,

  @Schema(required = true, description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
  @JsonProperty("refresh_token")
  val refreshToken: String,

  @Schema(required = true, description = "Token type is always bearer", example = "Bearer")
  @JsonProperty("token_type")
  val tokenType: String,

  @Schema(required = true, description = "Time for which access token is valid", example = "3600")
  @JsonProperty("expires_in")
  val expiresIn: Long,
)
