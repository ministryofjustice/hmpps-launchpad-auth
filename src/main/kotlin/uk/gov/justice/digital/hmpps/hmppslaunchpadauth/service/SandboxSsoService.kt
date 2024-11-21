package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims

class SandboxSsoService() {
  companion object {
    fun getThirdPartyTestUser(): UserClaims {
      val user = User("LaunchpadAuthTestUser", "John", "Smith")
      return UserClaims(null, null, user)
    }
  }
}
