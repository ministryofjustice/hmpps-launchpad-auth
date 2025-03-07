package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception

enum class ApiErrorTypes(private val error: String) {
  SERVER_ERROR("server_error"),
  ACCESS_DENIED("access_denied"),
  INVALID_REDIRECT_URI("invalid_redirectUri"),
  INVALID_CODE("invalid_code"),
  INVALID_SCOPE("invalid_scope"),
  INVALID_GRANT("invalid_grant"),
  INVALID_REQUEST("invalid_request"),
  INVALID_TOKEN("invalid_token"),
  EXPIRED_TOKEN("expired_token"), ;

  override fun toString(): String = error
}
