package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.PrisonEstablisments
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims
import java.util.*

@Service
class PrisonerApiServiceImpl(
  private var prisonApiClient: PrisonApiClient,
  @Qualifier("establishments")
  private var prisonEstablisments: PrisonEstablisments

) : PrisonerApiService {
  override fun getPrisonerData(prisonerId: String): UserClaims {
    val profile = prisonApiClient.getPrisonerProfileToken("G2320VD")
    val user = User(prisonerId, profile.lastName, profile.firstName, )
    val establishment = prisonEstablisments.establishment.filter { p -> p.agencyId == profile.agencyId }.get(0)
    val booking = Booking(profile.bookingId)
    return UserClaims(booking, establishment, user)
  }
}
