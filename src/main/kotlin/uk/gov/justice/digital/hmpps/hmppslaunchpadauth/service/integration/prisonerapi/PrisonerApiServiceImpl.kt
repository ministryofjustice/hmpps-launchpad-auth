package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import java.util.*

@Service
class PrisonerApiServiceImpl : PrisonerApiService{
  override fun getPrisonerData(prisonerId: String): UserClaims {
    val user = User(prisonerId, "Test", "User")
    val establishment = Establishment(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "HMPSS_London", "HMPSS_London", false)
    val booking = Booking(UUID.randomUUID().toString())
    return UserClaims(booking, establishment, user, )
  }
}
