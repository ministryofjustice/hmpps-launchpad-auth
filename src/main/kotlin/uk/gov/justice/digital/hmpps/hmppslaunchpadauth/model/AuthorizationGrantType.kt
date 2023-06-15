package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

enum class AuthorizationGrantType(private val grantType: String) {
  AUTHORIZATION_CODE("code"),
  REFRESH_TOKEN("refresh_token"), ;

  override fun toString(): String {
    return grantType
  }

  companion object {
    fun getEnumListStringValues(grants: Set<AuthorizationGrantType>): String {
      var values = ""
      grants.forEach { grant ->
        values = "$values,$grant"
      }
      return values
    }
  }
}
