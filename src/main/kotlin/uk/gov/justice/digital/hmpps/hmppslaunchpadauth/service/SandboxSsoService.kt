package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims

// @Service
class SandboxSsoService() {
  companion object {
    fun getThirdPartyTestUser(): UserClaims {
      val user = User("random_user@test.com", "John", "Smith")
      // val booking = Booking(UUID.randomUUID().toString())
      // val establishment = Establishment("EEI", "Test Establishment", "Test Establishment", false)
      return UserClaims(null, null, user)
    }
  }
}
