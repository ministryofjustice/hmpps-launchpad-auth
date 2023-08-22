package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Profile
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class IdTokenPayload : TokenPayload {

  private fun buildClaims(
    booking: Booking,
    establishment: Establishment,
    profile: Profile,
    clientId: UUID, scopes: Set<Scope>, nonce: String?): LinkedHashMap<String, Any> {
    var claims = LinkedHashMap<String, Any>()
    claims["name"] = "${profile.givenName} ${profile.familyName}"
    claims["given_name"] = profile.givenName
    claims["family_name"] = profile.familyName
    if (nonce != null) {
      claims["nonce"] = nonce
    }
    claims = TokenCommonClaims.buildCommonClaims(clientId.toString(), profile.id, claims)
    claims["exp"] = LocalDateTime.now().plusHours(12).toEpochSecond(ZoneOffset.UTC)
    if (scopes.contains(Scope.USER_BOOKING_READ)) {
      claims["booking"] = booking
    }
    if (scopes.contains(Scope.USER_ESTABLISHMENT_READ)) {
      claims["establishment"] = establishment
    }
    return claims
  }

  override fun generatePayload(
    booking: Booking?,
    establishment: Establishment?,
    profile: Profile,
    clientId: UUID,
    scopes: Set<Scope>,
    nonce: String?,
  ): LinkedHashMap<String, Any> {
    return buildClaims(booking!!, establishment!!, profile, clientId, scopes, nonce)
  }
}