package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

enum class Scope(val scope: String) {
  USER_BASIC_READ("user.basic.read"),
  USER_ESTABLISHMENT_READ("user.establishment.read"),
  USER_BOOKING_READ("user.booking.read"),
  USER_CLIENTS_READ("user.clients.read"),
  USER_CLIENTS_DELETE("user.clients.delete"), ;

  override fun toString(): String = scope

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

    fun getTemplateTextByScopes(scopes: Set<Scope>): Set<String?> {
      val template = HashSet<String?>()
      scopes.forEach { scope ->
        if (scope == USER_BASIC_READ) {
          template.add("Read basic information like your name")
        }
        if (scope == USER_BOOKING_READ) {
          template.add(null)
        }
        if (scope == USER_ESTABLISHMENT_READ) {
          template.add("Read prison information like the name of your prison")
        }
        if (scope == USER_CLIENTS_READ) {
          template.add("Read the list of applications you use")
        }
        if (scope == USER_CLIENTS_DELETE) {
          template.add("Remove access to applications you use")
        }
      }
      return template
    }

    fun getScopeDtosByScopes(scopes: Set<Scope>): List<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope> {
      val scopeDtos = ArrayList<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope>()
      if (scopes.contains(USER_BASIC_READ)) {
        scopeDtos.add(
          uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope(
            USER_BASIC_READ.toString(),
            "Read basic information like your name",
          ),
        )
      }
      if (scopes.contains(USER_BOOKING_READ)) {
        scopeDtos.add(
          uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope(
            USER_BOOKING_READ.toString(),
            null,
          ),
        )
      }
      if (scopes.contains(USER_ESTABLISHMENT_READ)) {
        scopeDtos.add(
          uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope(
            USER_ESTABLISHMENT_READ.toString(),
            "Read prison information like the name of your prison",
          ),
        )
      }
      if (scopes.contains(USER_CLIENTS_READ)) {
        scopeDtos.add(
          uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope(
            USER_CLIENTS_READ.toString(),
            "Read the list of applications you use",
          ),
        )
      }
      if (scopes.contains(USER_CLIENTS_DELETE)) {
        scopeDtos.add(
          uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope(
            USER_CLIENTS_DELETE.toString(),
            "Remove access to applications you use",
          ),
        )
      }
      return scopeDtos
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

    fun removeAllowListScopesNotRequired(scopesWithAllowList: String, allowListScopesNotRequired: List<String>): String {
      var scopes = scopesWithAllowList
      allowListScopesNotRequired.forEach { scope ->
        scopes = scopes.replace(scope, "")
      }
      return scopes.trim()
    }
  }
}
