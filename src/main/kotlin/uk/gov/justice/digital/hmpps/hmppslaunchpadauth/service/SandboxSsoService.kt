package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims

class SandboxSsoService {
  companion object {
    fun getThirdPartyTestUser(userId: String, scopes: Set<Scope>): UserClaims {
      val user = User(userId, "John", "Smith")
      var establishment: Establishment? = null
      if (scopes.contains(Scope.USER_ESTABLISHMENT_READ)) {
        establishment = Establishment(
          "HTE",
          "Test Establishment",
          "HMP TestEstablishment",
          false,
        )
      }
      return UserClaims(null, establishment, user)
    }
  }
}
