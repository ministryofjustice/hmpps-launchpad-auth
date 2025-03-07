package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import java.time.Instant
import java.util.*

class RefreshTokenPayload {
  private fun buildClaims(
    accessTokenId: String,
    user: User,
    clientId: UUID,
    scopes: Set<Scope>,
    validityInSeconds: Long,
  ): LinkedHashMap<String, Any> {
    var claims = LinkedHashMap<String, Any>()
    claims["jti"] = UUID.randomUUID().toString()
    claims["ati"] = accessTokenId
    claims = TokenCommonClaims.buildCommonClaims(clientId.toString(), user.id, claims)
    claims["exp"] = Instant.now().plusSeconds(validityInSeconds).epochSecond
    claims["scopes"] = TokenCommonClaims.buildScopeTextsSet(scopes)
    return claims
  }

  fun generatePayload(
    accessTokenId: String,
    user: User,
    clientId: UUID,
    scopes: Set<Scope>,
    validityInSeconds: Long,
  ): LinkedHashMap<String, Any> = buildClaims(accessTokenId, user, clientId, scopes, validityInSeconds)
}
