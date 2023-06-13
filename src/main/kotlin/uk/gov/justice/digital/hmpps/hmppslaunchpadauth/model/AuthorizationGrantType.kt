package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

enum class AuthorizationGrantType(private val grantType: String) {
  AUTHORIZATION_CODE("code"),
  REFRESH_TOKEN("refresh_token"), ;

  override fun toString(): String {
    return grantType
  }

  companion object {
    fun getStringValues(): String {
      return "$AUTHORIZATION_CODE,$REFRESH_TOKEN"
    }
  }
}
