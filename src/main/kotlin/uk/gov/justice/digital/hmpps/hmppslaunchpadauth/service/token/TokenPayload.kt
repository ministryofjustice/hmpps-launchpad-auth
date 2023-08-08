package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Booking
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Profile
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

abstract class TokenPayload {
  abstract fun generatePayload(
    booking: Booking?,
    establishment: Establishment?,
    profile: Profile,
    clientId: UUID,
    scopes: Set<Scope>,
    nonce: String?,
  ): HashMap<String, Any>

  fun buildHeaderClaims(alg: String, type: String): LinkedHashMap<String, Any> {
    val headerClaims = LinkedHashMap<String, Any>()
    headerClaims["alg"] = alg
    headerClaims["type"] = type
    return headerClaims
  }

  protected fun buildCommonClaims(
    aud: String,
    sub: String,
    payloadClaims: LinkedHashMap<String, Any>,
  ): LinkedHashMap<String, Any> {
    payloadClaims["iat"] = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
    payloadClaims["exp"] = LocalDateTime.now(ZoneOffset.UTC).plusHours(12).toEpochSecond(ZoneOffset.UTC)
    payloadClaims["aud"] = aud
    payloadClaims["sub"] = sub
    return payloadClaims
  }

  protected fun buildScopeTextsSet(scopes: Set<Scope>): Set<String> {
    val scopeTexts = HashSet<String>()
    scopes.forEach { scope ->
      scopeTexts.add(scope.toString())
    }
    return scopeTexts
  }
}
