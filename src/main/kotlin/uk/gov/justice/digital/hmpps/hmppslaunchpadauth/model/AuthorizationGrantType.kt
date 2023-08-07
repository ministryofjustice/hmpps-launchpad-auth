package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

import java.lang.IllegalArgumentException

enum class AuthorizationGrantType(private val grantType: String) {
  AUTHORIZATION_CODE("code"),
  REFRESH_TOKEN("refresh_token"), ;

  override fun toString(): String {
    return grantType
  }

  companion object {
    fun isStringMatchEnumValue(value: String, grants: Set<AuthorizationGrantType>): Boolean {
      grants.forEach { grant ->
        if (value == grant.toString()) {
          return true
        }
      }
      return false
    }

    fun getAuthorizationGrantTypeByStringValue(value: String): AuthorizationGrantType {
      AuthorizationGrantType.values().forEach { grantType ->
        if (value == grantType.toString()) {
          return grantType
        }
      }
      throw IllegalArgumentException("Invalid scope value")
    }
  }
}
