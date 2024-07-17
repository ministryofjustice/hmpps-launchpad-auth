package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class Token(
  @Schema(required = true, description = "This is a JWT that contains claims that carry information about the user. Clients should use the `id_token` to cache user information server-side.", example = "eyJhbGc..")
  @JsonProperty("id_token")
  val idToken: String,

  @Schema(required = true, description = "This is a JWT that contains claims that carry information about the authorised client.", example = "eyJhbGc..")
  @JsonProperty("access_token")
  val accessToken: String,

  @Schema(required = true, description = "This is a JWT that's used to renew or obtain a new token.", example = "eyJhbGc..")
  @JsonProperty("refresh_token")
  val refreshToken: String,

  @Schema(required = true, description = "Always set to `Bearer` to indicate that the token is a bearer token.", example = "Bearer")
  @JsonProperty("token_type")
  val tokenType: String,

  @Schema(required = true, description = "Seconds to indicate the duration of time the `access_token` is valid for. Clients should always use the expiry indicated in the JWTs through the `exp` claim.", example = "3599")
  @JsonProperty("expires_in")
  val expiresIn: Long,
)
