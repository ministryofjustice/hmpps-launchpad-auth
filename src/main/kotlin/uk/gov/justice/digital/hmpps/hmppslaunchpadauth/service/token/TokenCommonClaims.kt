package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import java.time.Instant

class TokenCommonClaims {
  companion object {
    fun buildHeaderClaims(kid: String): LinkedHashMap<String, Any> {
      val headerClaims = LinkedHashMap<String, Any>()
      headerClaims["alg"] = "RS256"
      headerClaims["typ"] = "JWT"
      headerClaims["kid"] = kid
      return headerClaims
    }

    fun buildCommonClaims(
      aud: String,
      sub: String,
      payloadClaims: LinkedHashMap<String, Any>,
    ): LinkedHashMap<String, Any> {
      payloadClaims["iat"] = Instant.now().epochSecond
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
