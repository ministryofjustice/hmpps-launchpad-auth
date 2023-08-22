package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Profile
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class RefreshTokenPayload : TokenPayload{
  private fun buildClaims(
    profile: Profile,
    clientId: UUID, scope: Set<Scope>): LinkedHashMap<String, Any> {
    var claims = LinkedHashMap<String, Any>()
    claims["jti"] = UUID.randomUUID().toString()
    claims = TokenCommonClaims.buildCommonClaims(clientId.toString(), profile.id, claims)
    claims["exp"] = LocalDateTime.now().plusDays(7).toEpochSecond(ZoneOffset.UTC)
    claims["scopes"] = TokenCommonClaims.buildScopeTextsSet(scope)
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
    return buildClaims(profile, clientId, scopes)
  }
}
