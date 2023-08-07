package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import java.util.*

open class AuthenticationInfo(
  open val clientId: UUID,
  open val clientScope: Set<Scope>
)
