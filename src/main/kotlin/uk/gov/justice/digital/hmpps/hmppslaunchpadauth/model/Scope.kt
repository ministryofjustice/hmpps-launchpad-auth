package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

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
  }
}
