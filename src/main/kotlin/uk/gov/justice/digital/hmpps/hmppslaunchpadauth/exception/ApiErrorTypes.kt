package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception

enum class ApiErrorTypes(private val error: String) {
  SERVER_ERROR("server_error"),
  ACCESS_DENIED("access_denied"),
  INVALID_REDIRECT_URI("invalid_redirectUri"),
  INVALID_CODE("invalid_code"),
  INVALID_SCOPE("invalid_scope"),
  INVALID_GRANT("invalid_grant"),
  INVALID_REQUEST("invalid_request"),
  EXPIRE_ACCESS_TOKEN("expire_access_token"),
  EXPIRE_REFRESH_TOKEN("expire_refresh_token"),
  UNAUTHORIZED("invalid_authorization");
  override fun toString(): String {
    return error
  }
}