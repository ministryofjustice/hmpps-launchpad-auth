package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Profile
import java.util.*
import kotlin.collections.HashMap

@Component
class IdTokenPayload: TokenPayload() {

  private fun buildClaims(
    booking: Booking,
    establishment: Establishment,
    profile: Profile,
    clientId: UUID, scope: Set<Scope>,  nonce: String?): HashMap<String, Any> {
    var claims = LinkedHashMap<String, Any>()
    claims["name"] = profile.name
    claims["given_name"] = profile.givenName
    claims["family_name"] = profile.familyName
    //claims["profile"] = ""
    //claims["picture"] = ""
    if (nonce != null) {
      claims["nonce"] = nonce
    }
    claims = buildCommonClaims(clientId.toString(), profile.id, claims)
    if (scope.contains(Scope.USER_BASIC_READ)) {
      claims["booking"] = booking
    }
    claims["establishment"] = establishment
    return claims
  }

  override fun generatePayload(
    booking: Booking?,
    establishment: Establishment?,
    profile: Profile,
    clientId: UUID,
    scopes: Set<Scope>,
    nonce: String?,
  ): HashMap<String, Any> {
//    TODO("Not yet implemented")
    return buildClaims(booking!!, establishment!!, profile!!, clientId, scopes, nonce)
  }

}