package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

import java.lang.IllegalArgumentException

enum class Scope(val scope: String) {
  USER_BASIC_READ("user.basic.read"),
  USER_ESTABLISHMENT_READ("user.establishment.read"),
  USER_BOOKING_READ("user.booking.read"),
  USER_CLIENTS_READ("user.clients.read"),
  USER_CLIENTS_DELETE("user.clients.delete"), ;
  override fun toString(): String {
    return scope
  }

  companion object {
    fun isStringMatchEnumValue(value: String, scopes: Set<Scope>): Boolean {
      scopes.forEach { scope ->
        if (value == scope.toString()) {
          return true
        }
      }
      return false
    }

    fun getScopesByValues(values: Set<String>): Set<Scope> {
      val scopes = HashSet<Scope>()
      values.forEach { value ->
        scopes.add(getScopeByStringValue(value))
      }
      return scopes
    }

    fun getTemplateTextByScopes(scopes: Set<Scope>): Set<String> {
      val template = HashSet<String>()
      scopes.forEach { scope ->
        if (scope == USER_BASIC_READ) {
          template.add("Your name")
        }
        if (scope == USER_BOOKING_READ) {
          template.add("Prison booking details (tbc)")
        }
        if (scope == USER_ESTABLISHMENT_READ) {
          template.add("Details of your prison")
        }
        if (scope == USER_CLIENTS_READ) {
          template.add("Apps you’ve allowed access to")
        }
        if (scope == USER_CLIENTS_DELETE) {
          template.add("Apps you’ve removed access to")
        }
      }
      return template
    }

    fun cleanScopes(scopes: String): Set<String> {
      val scopeList: List<String>
      if (scopes.contains(" ")) {
        val scopeValues = scopes.replace("\\s+".toRegex(), " ")
        scopeList = scopeValues.split("\\s".toRegex())
        return java.util.HashSet(scopeList)
      } else {
        return setOf(scopes)
      }
    }

    fun getScopeByStringValue(value: String): Scope {
      Scope.values().forEach { scope ->
        if (value == scope.toString()) {
          return scope
        }
      }
      throw IllegalArgumentException("Invalid scope value $value")
    }

    fun removeWhitelistedScopes(scopesWithWhitelistedScopes: String): String {
      var scopes = scopesWithWhitelistedScopes
      scopes  = scopes.replace("openid", "")
      scopes = scopes.replace("profile", "")
      scopes = scopes.replace("email", "")
      scopes = scopes.trimStart()
      return scopes
    }
  }
}
