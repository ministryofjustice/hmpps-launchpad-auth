package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims
import java.util.*

interface PrisonerApiService {
  fun getPrisonerData(prisonerId: String, clientId: UUID): UserClaims
}
