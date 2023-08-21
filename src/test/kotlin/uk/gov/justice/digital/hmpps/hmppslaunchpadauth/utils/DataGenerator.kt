package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Profile
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.AccessTokenPayload
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenGenerationAndValidation
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenCommonClaims
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

const val REDIRECT_URI = "https://launchpad.com"
const val LOGO_URI = "$REDIRECT_URI/logo"
const val USER_ID = "G2320VD"

class DataGenerator {
  val CLIENT_ID = UUID.randomUUID()

  companion object {
    fun buildClient(enabled: Boolean, autoApprove: Boolean): Client {
      return Client(
        UUID.randomUUID(),
        UUID.randomUUID().toString(),
        setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ, Scope.USER_ESTABLISHMENT_READ),
        setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN),
        setOf(REDIRECT_URI),
        enabled,
        autoApprove,
        "Test App",
        LOGO_URI,
        "Test App for test environment",
      )
    }

    fun buildSsoRequest(): SsoRequest {
      return SsoRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
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

    fun jwtBuilder(issue: Instant, exp: Instant, nonce: UUID, userId: String?): String {
      val issueDate = Date.from(issue)
      val expDate = Date.from(exp)
      return Jwts.builder()
        .setIssuer("RandomIssuer")
        .setSubject("login")
        .setAudience("test audience")
        // .claim("preferred_username", "testuser@test.com")
        .claim("name", "Test User")
        .claim("scope", "openid")
        .claim("nonce", nonce)
        .claim("email", userId)
        .setIssuedAt(issueDate)
        .setExpiration(expDate)
        .signWith(
          SignatureAlgorithm.HS256,
          "random secret",
        )
        .compact()
    }

    fun buildUserApprovedClient(
      userId: String,
      clientId: UUID,
      scopes: Set<Scope>,
      createdDate: LocalDateTime,
      lastModifiedDate: LocalDateTime,
    ): UserApprovedClient {
      return UserApprovedClient(
        UUID.randomUUID(),
        userId,
        clientId,
        scopes,
        createdDate,
        lastModifiedDate,
      )
    }

    fun createValidAccessToken() {
      val password = UUID.randomUUID().toString()
      val scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
      val grants = setOf(AuthorizationGrantType.AUTHORIZATION_CODE)
      val redirectUri = REDIRECT_URI
      val logoUri = LOGO_URI
      val clientId = UUID.randomUUID()
      val randomSecret = UUID.randomUUID().toString()
      val client = Client(
        clientId,
        BCryptPasswordEncoder().encode(password),
        scopes,
        grants,
        setOf(redirectUri),
        true,
        true,
        "Test App",
        logoUri,
        "Test Description",
      )
      val userApprovedScopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
      val userApprovedClient = UserApprovedClient(
        UUID.randomUUID(),
        USER_ID,
        clientId,
        userApprovedScopes,
        LocalDateTime.now(ZoneOffset.UTC),
        LocalDateTime.now(ZoneOffset.UTC),
      )
      val accessTokenPayload = AccessTokenPayload()
      val nonce = "random_nonce"
      val payload = accessTokenPayload.generatePayload(
        null,
        null,
        Profile(USER_ID, "John Smith", "John", "Smith"),
        clientId,
        userApprovedScopes,
        nonce,
      )
      val authHeader = "Bearer " + TokenGenerationAndValidation.generateToken(
        payload,
        TokenCommonClaims.buildHeaderClaims(SignatureAlgorithm.HS256.toString(), "JWT"),
        randomSecret,
      )
    }

    fun generateAccessToken(
      client: Client,
      userApprovedClient: UserApprovedClient,
      nonce: String?,
      secret: String,
    ): String {
      val accessTokenPayload = AccessTokenPayload()
      val nonce = "random_nonce"
      val payload = accessTokenPayload.generatePayload(
        null,
        null,
        Profile(USER_ID, "John Smith", "John", "Smith"),
        client.id,
        userApprovedClient.scopes,
        nonce,
      )
      return "Bearer " + TokenGenerationAndValidation.generateToken(
        payload,
        TokenCommonClaims.buildHeaderClaims(SignatureAlgorithm.HS256.toString(), "JWT"),
        secret,
      )
    }
  }
}
