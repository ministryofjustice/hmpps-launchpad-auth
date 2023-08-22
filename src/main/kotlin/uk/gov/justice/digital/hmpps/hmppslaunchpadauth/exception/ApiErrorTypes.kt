package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception

enum class ApiErrorTypes(private val error: String) {
  SERVER_ERROR("server_error"),
  ACCESS_DENIED("access_denied"),
  INVALID_REDIRECT_URI("invalid_redirectUri"),
  INVALID_CODE("invalid_code"),
  INVALID_CLIENT("invalid_client"),
  INVALID_SCOPE("invalid_scope"),
  INVALID_RESPONSE_TYPE("invalid_response_type"),
  INVALID_GRANT("invalid_grant"),
  INVALID_REQUEST("invalid_request"),
  INVALID_TOKEN("invalid_token"),
  EXPIRED_ACCESS_TOKEN("expired_access_token"),
  EXPIRED_REFRESH_TOKEN("expired_refresh_token"),
  UNAUTHORIZED("invalid_authorization");
  override fun toString(): String {
    return error
  }
}