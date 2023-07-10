package uk.gov.justice.digital.hmpps.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class DataGenerator {
  companion object {
    fun buildClient(enabled: Boolean, autoApprove: Boolean): Client {
      return Client(
        UUID.randomUUID(),
        UUID.randomUUID().toString(),
        setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ, Scope.USER_ESTABLISHMENT_READ),
        setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN),
        setOf("http://localhost:8080/test"),
        enabled,
        autoApprove,
        "Test App",
        "http://localhost:8080/test",
        "Update Test App",
      )
    }

    fun buildSsoRequest(): SsoRequest {
      return SsoRequest(
        UUID.randomUUID(),
        UUID.randomUUID().toString(),
        LocalDateTime.now(ZoneOffset.UTC),
        UUID.randomUUID(),
        SsoClient(
          UUID.randomUUID(),
          UUID.randomUUID().toString(),
          UUID.randomUUID().toString(),
          setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ),
          "http://localhost:8080/test",
        ),
        "testuser@test.com",
      )
    }

    fun jwtBuilder(issue: Instant, exp: Instant, nonce: UUID, userId: UUID): String {
      val issueDate = Date.from(issue)
      val expDate = Date.from(exp)
      return Jwts.builder()
        .setIssuer("RandomIssuer")
        .setSubject("login")
        .setAudience("test audience")
        //.claim("preferred_username", "testuser@test.com")
        .claim("name", "Varun Kumar")
        .claim("scope", "openid")
        .claim("nonce", nonce)
        .claim("oid", userId)
        .setIssuedAt(issueDate)
        .setExpiration(expDate)
        .signWith(
          SignatureAlgorithm.HS256,
          "random secret",
        )
        .compact()
    }
  }
}
