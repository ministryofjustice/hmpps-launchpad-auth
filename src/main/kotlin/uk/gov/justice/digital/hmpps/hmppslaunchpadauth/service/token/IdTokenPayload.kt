package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class IdTokenPayload {

  private fun buildClaims(
    booking: Booking,
    establishment: Establishment,
    user: User,
    clientId: UUID,
    scopes: Set<Scope>,
    nonce: String?,
    issuerUrl: String,
  ): LinkedHashMap<String, Any> {
    var claims = LinkedHashMap<String, Any>()
    claims["name"] = "${user.givenName} ${user.familyName}"
    claims["given_name"] = user.givenName
    claims["family_name"] = user.familyName
    if (nonce != null) {
      claims["nonce"] = nonce
    }
    claims = TokenCommonClaims.buildCommonClaims(clientId.toString(), user.id, claims)
    claims["exp"] = LocalDateTime.now().plusHours(12).toEpochSecond(ZoneOffset.UTC)
    claims["iss"] = issuerUrl
    if (scopes.contains(Scope.USER_BOOKING_READ)) {
      claims["booking"] = booking
    }
    if (scopes.contains(Scope.USER_ESTABLISHMENT_READ)) {
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
  ): LinkedHashMap<String, Any> {
    return buildClaims(booking!!, establishment!!, user, clientId, scopes, nonce, issuerUrl)
  }
}
