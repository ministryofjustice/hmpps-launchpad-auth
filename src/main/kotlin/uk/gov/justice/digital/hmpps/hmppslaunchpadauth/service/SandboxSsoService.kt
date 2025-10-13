package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims

class SandboxSsoService {
  companion object {
    fun getThirdPartyTestUser(userId: String, scopes: Set<Scope>): UserClaims {
      val user = User(userId, "John", "Smith")
      return when {
        scopes.contains(Scope.USER_BOOKING_READ) -> {
          val booking = Booking("12345")
          val establishment = Establishment(
            "BNI",
            "Test Establishment",
            "HMP TestEstablishment",
            false,
          )
          UserClaims(booking, establishment, user)
        }
        scopes.contains(Scope.USER_ESTABLISHMENT_READ) -> {
          val establishment = Establishment(
            "HTE",
            "Test Establishment",
            "HMP TestEstablishment",
            false,
          )
          UserClaims(null, establishment, user)
        }
        else -> UserClaims(null, null, user)
      }
    }
  }
}
