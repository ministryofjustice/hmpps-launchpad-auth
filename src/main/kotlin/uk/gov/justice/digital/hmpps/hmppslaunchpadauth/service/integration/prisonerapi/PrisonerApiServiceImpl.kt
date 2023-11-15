package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.OffenderBooking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.PrisonEstablishments
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims
import java.util.*

@Service
class PrisonerApiServiceImpl(
  private var offenderBooking: OffenderBooking,
  @Qualifier("establishments")
  private var prisonEstablishments: PrisonEstablishments,
) : PrisonerApiService {
  override fun getPrisonerData(prisonerId: String): UserClaims {
    val profile = offenderBooking.getOffenderBooking(prisonerId)
    val user = User(prisonerId, profile.lastName, profile.firstName)
    val establishment = prisonEstablishments.agencies.get(profile.agencyId)
    if (establishment == null) {
      val message = "Establishment not yet implemented for agency id ${profile.agencyId}"
      throw ApiException(message, HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), ApiErrorTypes.SERVER_ERROR.toString())
    }
    val booking = Booking(profile.bookingId)
    return UserClaims(booking, establishment!!, user)
  }
}
