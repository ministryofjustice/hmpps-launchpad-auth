package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

enum class AuthorizationGrantType(private val grantType: String) {
  AUTHORIZATION_CODE("authorization_code"),
  REFRESH_TOKEN("refresh_token");

  override fun toString(): String {
    return grantType;
  }
}