package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import java.time.LocalDateTime
import java.time.ZoneOffset

class TokenCommonClaims {
  companion object {
    fun buildHeaderClaims(): LinkedHashMap<String, Any> {
      val headerClaims = LinkedHashMap<String, Any>()
      headerClaims["alg"] = "HS256"
      headerClaims["typ"] = "JWT"
      return headerClaims
    }

    fun buildCommonClaims(
      aud: String,
      sub: String,
      payloadClaims: LinkedHashMap<String, Any>,
    ): LinkedHashMap<String, Any> {
      payloadClaims["iat"] = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
      payloadClaims["aud"] = aud
      payloadClaims["sub"] = sub
      return payloadClaims
    }

    fun buildScopeTextsSet(scopes: Set<Scope>): Set<String> {
      val scopeTexts = HashSet<String>()
      scopes.forEach { scope ->
        scopeTexts.add(scope.toString())
      }
      return scopeTexts
    }
  }
}
