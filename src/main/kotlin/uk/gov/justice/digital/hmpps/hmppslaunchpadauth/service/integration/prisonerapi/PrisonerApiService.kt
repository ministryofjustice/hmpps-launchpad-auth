package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.PrisonerData

interface PrisonerApiService {
  fun getPrisonerData(prisonerId: String): PrisonerData
}