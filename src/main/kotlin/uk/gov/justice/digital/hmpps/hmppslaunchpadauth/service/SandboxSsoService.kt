package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims

class SandboxSsoService() {
  companion object {
    fun getThirdPartyTestUser(userId: String): UserClaims {
      val user = User(userId, "John", "Smith")
      return UserClaims(null, null, user)
    }
  }
}
