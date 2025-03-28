package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

enum class AuthorizationGrantType(private val grantType: String) {
  AUTHORIZATION_CODE("authorization_code"),
  REFRESH_TOKEN("refresh_token"), ;

  override fun toString(): String = grantType

  companion object {

    fun getAuthorizationGrantTypeByStringValue(value: String): AuthorizationGrantType {
      AuthorizationGrantType.values().forEach { grantType ->
        if (value == grantType.toString()) {
          return grantType
        }
      }
      throw IllegalArgumentException(String.format("Invalid grant value %s", value))
    }
  }
}
