package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import java.util.*

interface TokenPayload {
  fun generatePayload(
    booking: Booking?,
    establishment: Establishment?,
    user: User,
    clientId: UUID,
    scopes: Set<Scope>,
    nonce: String?,
  ): LinkedHashMap<String, Any>
}