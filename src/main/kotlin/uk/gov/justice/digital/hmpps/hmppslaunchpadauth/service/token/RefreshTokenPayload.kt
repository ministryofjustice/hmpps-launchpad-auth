package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Profile
import java.util.*

@Component
class RefreshTokenPayload: TokenPayload() {
  private fun buildClaims(
    booking: Booking?,
    establishment: Establishment?,
    profile: Profile,
    clientId: UUID, scope: Set<Scope>,  nonce: String?): LinkedHashMap<String, Any> {
    var claims = LinkedHashMap<String, Any>()
    claims = buildCommonClaims(clientId.toString(), profile.id, claims)
    claims["scope"] = buildScopeTextsSet(scope)
    if (nonce != null) {
      claims["nonce"] = nonce
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
    return buildClaims(null, null, profile, clientId, scopes, nonce)
  }
}
