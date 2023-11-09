package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.PrisonEstablishments
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims
import java.util.*

@Service
class PrisonerApiServiceImpl(
  private var prisonApiClient: PrisonApiClient,
  @Qualifier("establishments")
  private var prisonEstablishments: PrisonEstablishments

) : PrisonerApiService {
  override fun getPrisonerData(prisonerId: String): UserClaims {
    val profile = prisonApiClient.getPrisonerProfileToken(prisonerId)
    val user = User(prisonerId, profile.lastName, profile.firstName, )
    val establishment = prisonEstablishments.agencies.get(profile.agencyId)
    val booking = Booking(profile.bookingId)
    return UserClaims(booking, establishment!!, user)
  }

}
