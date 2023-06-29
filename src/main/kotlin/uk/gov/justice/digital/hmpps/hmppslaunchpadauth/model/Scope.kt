package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException

enum class Scope(val scope: String) {
  USER_BASIC_READ("user.basic.read"),
  USER_ESTABLISHMENT_READ("user.establishment.read"),
  USER_BOOKING_READ("user.booking.read"), ;

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

    fun getEnumByStringValue(value: String): Scope {
      Scope.values().forEach { scope ->
        if (value == scope.toString()) {
          return scope
        }
      }
      throw ApiException("Invalid scope value", 400)
    }

    fun getEnumsByValues(values: Set<String>): Set<Scope> {
      val scopes = HashSet<Scope>()
      values.forEach { value ->
        scopes.add(getEnumByStringValue(value))
      }
      return scopes
    }

    fun getTemplateTextByEnums(scopes: Set<Scope>): Set<String> {
      val template =  HashSet<String>()
      scopes.forEach { scope ->
        if (scope == USER_BASIC_READ) {
          template.add("Read basic information like your name")
        }
        if (scope == USER_BOOKING_READ) {
          template.add("Read  your booking information")
        }
        if (scope == USER_ESTABLISHMENT_READ) {
          template.add("Read  your establishment information")
        }
      }
      return template;
    }
  }
}
