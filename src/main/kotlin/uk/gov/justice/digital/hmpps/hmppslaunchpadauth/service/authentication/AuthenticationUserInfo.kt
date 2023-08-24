package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import java.util.*

class AuthenticationUserInfo(
  override val clientId: UUID,
  val userId: String,
  val userApprovedScope: Set<Scope>,
  ): AuthenticationInfo(clientId)
