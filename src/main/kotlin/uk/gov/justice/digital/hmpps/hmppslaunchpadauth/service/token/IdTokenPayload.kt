package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import java.time.Instant
import java.util.*

class IdTokenPayload {

  private fun buildClaims(
    booking: Booking?,
    establishment: Establishment?,
    user: User,
    clientId: UUID,
    scopes: Set<Scope>,
    nonce: String?,
    issuerUrl: String,
    validityInSeconds: Long,
  ): LinkedHashMap<String, Any> {
    var claims = LinkedHashMap<String, Any>()
    if (scopes.contains(Scope.USER_BASIC_READ)) {
      claims["name"] = "${user.givenName} ${user.familyName}"
      claims["given_name"] = user.givenName
      claims["family_name"] = user.familyName
    }
    if (nonce != null) {
      claims["nonce"] = nonce
    }
    claims = TokenCommonClaims.buildCommonClaims(clientId.toString(), user.id, claims)
    claims["exp"] = Instant.now().plusSeconds(validityInSeconds).epochSecond
    claims["iss"] = issuerUrl
    if (scopes.contains(Scope.USER_BOOKING_READ) && booking != null) {
      claims["booking"] = booking
    }
    if (scopes.contains(Scope.USER_ESTABLISHMENT_READ) && establishment != null) {
      claims["establishment"] = establishment
    }
    return claims
  }

  fun generatePayload(
    booking: Booking?,
    establishment: Establishment?,
    user: User,
    clientId: UUID,
    scopes: Set<Scope>,
    nonce: String?,
    issuerUrl: String,
    validityInSeconds: Long,
  ): LinkedHashMap<String, Any> = buildClaims(booking, establishment, user, clientId, scopes, nonce, issuerUrl, validityInSeconds)
}
