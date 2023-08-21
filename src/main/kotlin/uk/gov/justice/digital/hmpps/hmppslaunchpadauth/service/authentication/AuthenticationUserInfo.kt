package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import java.util.*

class AuthenticationUserInfo(
  override val clientId: UUID,
  // override val clientScope: Set<Scope>,
  val userId: String,
  val userApprovedScope: Set<Scope>,
  ): AuthenticationInfo(clientId)
