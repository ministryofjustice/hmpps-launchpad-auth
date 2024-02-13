package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import java.time.LocalDateTime
import java.time.ZoneOffset
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
    claims["exp"] = LocalDateTime.now().plusSeconds(validityInSeconds).toEpochSecond(ZoneOffset.UTC)
    claims["scopes"] = TokenCommonClaims.buildScopeTextsSet(scopes)
    return claims
  }

  fun generatePayload(
    accessTokenId: String,
    user: User,
    clientId: UUID,
    scopes: Set<Scope>,
    validityInSeconds: Long,
  ): LinkedHashMap<String, Any> {
    return buildClaims(accessTokenId, user, clientId, scopes, validityInSeconds)
  }
}
