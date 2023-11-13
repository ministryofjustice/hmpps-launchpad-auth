package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims

interface PrisonerApiService {
  fun getPrisonerData(prisonerId: String): UserClaims
}
